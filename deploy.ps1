# deploy.ps1 - Version corrigée et testée
param (
    [string]$projectPath = "C:\Users\dinap\OneDrive\Bureau\ESI\2CS\S2\Projet 2CS\test\test_j2ee",
    [string]$tomeePath = "C:\apache-tomee-webprofile-10.0.1",
    [string]$warName = "test_j2ee.war"
)

Write-Host "=== DÉPLOIEMENT AUTOMATIQUE TOMEE ===" -ForegroundColor Yellow
Write-Host "Projet : $projectPath" -ForegroundColor Cyan

# 1. Build Maven
try {
    Write-Host "`n[1/4] COMPILATION AVEC MAVEN..." -ForegroundColor Blue
    cd $projectPath
    mvn clean package

    if ($LASTEXITCODE -ne 0) {
        throw "Erreur Maven (code $LASTEXITCODE)"
    }
}
catch {
    Write-Host "ERREUR: $_" -ForegroundColor Red
    exit 1
}

# 2. Arrêt de TomEE
try {
    Write-Host "`n[2/4] ARRÊT DE TOMEE..." -ForegroundColor Blue
    $tomeeProcess = Get-Process -Name "java" -ErrorAction SilentlyContinue |
            Where-Object { $_.Path -like "*$tomeePath*" }

    if ($tomeeProcess) {
        Write-Host "-> Arrêt du processus TomEE (PID $($tomeeProcess.Id))"
        Stop-Process -Id $tomeeProcess.Id -Force
        Start-Sleep -Seconds 5
    }
    else {
        Write-Host "-> TomEE n'était pas démarré" -ForegroundColor DarkGray
    }
}
catch {
    Write-Host "ERREUR lors de l'arrêt de TomEE: $_" -ForegroundColor Red
}

# 3. Nettoyage
try {
    Write-Host "`n[3/4] NETTOYAGE DE WEBAPPS..." -ForegroundColor Blue
    $webappsPath = "$tomeePath\webapps"
    $warPath = "$webappsPath\$warName"
    $extractedFolder = "$webappsPath\test_j2ee"

    if (Test-Path $warPath) {
        Remove-Item $warPath -Force
        Write-Host "-> Fichier WAR existant supprimé"
    }

    if (Test-Path $extractedFolder) {
        Remove-Item $extractedFolder -Recurse -Force
        Write-Host "-> Dossier déployé existant supprimé"
    }
}
catch {
    Write-Host "ERREUR lors du nettoyage: $_" -ForegroundColor Red
}

# 5. Déploiement
try {
    Write-Output "`n[4/4] DEPLOIEMENT..."
    $warFile = "$projectPath\target\$warName"

    if (-not (Test-Path $warFile)) {
        throw "Fichier WAR introuvable"
    }

    Copy-Item $warFile -Destination "$tomeePath\webapps\" -Force

    # CHANGEMENT ICI - Lancement persistent de TomEE
    $tomeeBin = "$tomeePath\bin"
    Start-Process -FilePath "cmd.exe" -ArgumentList "/c `"cd `"$tomeeBin`" && startup.bat`"" -Wait

    Write-Output "`nSUCCES: Déploiement terminé!"
    Write-Output "Application disponible sur: http://localhost:8090/test_j2ee"

    # Optionnel: Ouvre le navigateur automatiquement
    #Start-Process "http://localhost:8090/test_j2ee"
}
catch {
    Write-Output "ERREUR: $_"
    exit 1
}