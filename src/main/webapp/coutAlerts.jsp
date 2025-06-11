<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Alertes de Coûts d'Opérations</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .alert-header {
            padding: 15px;
            background-color: #f8f9fa;
            margin-bottom: 20px;
            border-radius: 5px;
        }
        .alert-card {
            margin-bottom: 20px;
            border-radius: 5px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
        .overrun {
            border-left: 5px solid #dc3545;
        }
        .monitor {
            border-left: 5px solid #ffc107;
        }
        .cost-table th {
            background-color: #f8f9fa;
        }
    </style>
</head>
<body>
    <div class="container-fluid py-4">
        <div class="row alert-header">
            <div class="col-md-8">
                <h1>Alertes de Coûts d'Opérations</h1>
                <p class="text-muted">Surveillance des dépassements et coûts à surveiller</p>
            </div>
            <div class="col-md-4 text-end">
                <a href="coutAlerts" class="btn btn-primary">
                    <i class="bi bi-arrow-clockwise"></i> Rafraîchir
                </a>
            </div>
        </div>

        <!-- Cartes de résumé -->
        <div class="row mb-4">
            <div class="col-md-6">
                <div class="card bg-danger text-white">
                    <div class="card-body">
                        <h5 class="card-title">Dépassements</h5>
                        <h2 class="display-4">${totalOverruns}</h2>
                        <p class="card-text">Coûts en statut "Dépassement"</p>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="card bg-warning text-dark">
                    <div class="card-body">
                        <h5 class="card-title">À Surveiller</h5>
                        <h2 class="display-4">${totalToMonitor}</h2>
                        <p class="card-text">Coûts en statut "À surveiller"</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- Section des alertes de dépassement -->
        <div class="row">
            <div class="col-12">
                <div class="card alert-card overrun">
                    <div class="card-header bg-white d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">Coûts en Dépassement</h5>
                        <span class="badge bg-danger">${totalOverruns}</span>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${empty overrunCosts}">
                                <p class="text-center text-muted">Aucun coût en dépassement</p>
                            </c:when>
                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table table-striped table-hover cost-table">
                                        <thead>
                                            <tr>
                                                <th>Opération</th>
                                                <th>Puits</th>
                                                <th>Phase</th>
                                                <th>Date</th>
                                                <th>Coût Prévu</th>
                                                <th>Coût Réel</th>
                                                <th>Statut</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach items="${overrunCosts}" var="cost">
                                                <tr>
                                                    <td>${cost.nomOpr}</td>
                                                    <td>${cost.nom_puit}</td>
                                                    <td>${cost.phase}</td>
                                                    <td><fmt:formatDate value="${cost.date}" pattern="dd/MM/yyyy"/></td>
                                                    <td><fmt:formatNumber value="${cost.coutPrevu}" type="currency" currencySymbol="$"/></td>
                                                    <td><fmt:formatNumber value="${cost.coutReel}" type="currency" currencySymbol="$"/></td>
                                                    <td><span class="badge bg-danger">${cost.statutCout}</span></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>

        <!-- Section des coûts à surveiller -->
        <div class="row">
            <div class="col-12">
                <div class="card alert-card monitor">
                    <div class="card-header bg-white d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">Coûts à Surveiller</h5>
                        <span class="badge bg-warning text-dark">${totalToMonitor}</span>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${empty monitorCosts}">
                                <p class="text-center text-muted">Aucun coût à surveiller</p>
                            </c:when>
                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table table-striped table-hover cost-table">
                                        <thead>
                                            <tr>
                                                <th>Opération</th>
                                                <th>Puits</th>
                                                <th>Phase</th>
                                                <th>Date</th>
                                                <th>Coût Prévu</th>
                                                <th>Coût Réel</th>
                                                <th>Statut</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach items="${monitorCosts}" var="cost">
                                                <tr>
                                                    <td>${cost.nomOpr}</td>
                                                    <td>${cost.nom_puit}</td>
                                                    <td>${cost.phase}</td>
                                                    <td><fmt:formatDate value="${cost.date}" pattern="dd/MM/yyyy"/></td>
                                                    <td><fmt:formatNumber value="${cost.coutPrevu}" type="currency" currencySymbol="$"/></td>
                                                    <td><fmt:formatNumber value="${cost.coutReel}" type="currency" currencySymbol="$"/></td>
                                                    <td><span class="badge bg-warning text-dark">${cost.statutCout}</span></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Bootstrap Bundle with Popper -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>