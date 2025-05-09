<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Import Excel</title>
    <style>
        .form-container {
            max-width: 500px;
            margin: 30px auto;
            padding: 20px;
            border: 1px solid #ddd;
            border-radius: 5px;
        }
        .radio-group { margin: 20px 0; }
        .radio-option { margin: 10px 0; }
        .btn-submit {
            background: #2196F3;
            color: white;
            padding: 10px 15px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
    </style>
</head>
<body>
    <div class="form-container">
        <h2>Importer un fichier Excel</h2>
        <form action="upload-excel" method="post" enctype="multipart/form-data">
            <div class="radio-group">
                <div class="radio-option">
                    <input type="radio" id="dailyCost" name="reportType" value="dailyCost" checked>
                    <label for="dailyCost">Coûts Journaliers (feuille 'Daily Cost')</label>
                </div>
                <div class="radio-option">
                    <input type="radio" id="drilling" name="reportType" value="drilling">
                    <label for="drilling">Rapport de Forage (1ère feuille)</label>
                </div>
            </div>

            <input type="file" name="excelFile" accept=".xlsx,.xls" required><br><br>
            <button type="submit" class="btn-submit">Importer</button>
        </form>
    </div>
</body>
</html>