<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Drilling Parameters Dashboard</title>
    <style>
        .dashboard {
            font-family: Arial, sans-serif;
            margin: 20px;
            padding: 20px;
            border: 1px solid #ddd;
            border-radius: 5px;
            max-width: 800px;
        }
        .parameter {
            margin: 10px 0;
            padding: 10px;
            background-color: #f5f5f5;
            border-radius: 4px;
        }
        .parameter-label {
            font-weight: bold;
            color: #2196F3;
        }
    </style>
</head>
<body>
    <div class="dashboard">
        <h1>Drilling Parameters</h1>

        <c:if test="${not empty drillingParams}">
            <div class="parameter">
                <span class="parameter-label">Bit Number:</span>
                ${drillingParams.bitNumber}
            </div>

            <div class="parameter">
                <span class="parameter-label">Bit Size:</span>
                ${drillingParams.bitSize}
            </div>

            <div class="parameter">
                <span class="parameter-label">WOB Min/Max:</span>
                ${drillingParams.wobMin} / ${drillingParams.wobMax} tonnes
            </div>

            <div class="parameter">
                <span class="parameter-label">RPM Min/Max:</span>
                ${drillingParams.rpmMin} / ${drillingParams.rpmMax}
            </div>

            <div class="parameter">
                <span class="parameter-label">Flow Rate:</span>
                ${drillingParams.flowRate} GPM
            </div>

            <div class="parameter">
                <span class="parameter-label">Pressure:</span>
                ${drillingParams.pressure} PSI
            </div>

            <div class="parameter">
                <span class="parameter-label">HSI:</span>
                ${drillingParams.hsi} HP/sq.in
            </div>

            <div class="parameter">
                <span class="parameter-label">Depth:</span>
                ${drillingParams.depth} meters
            </div>
        </c:if>

        <c:if test="${empty drillingParams}">
            <p>No drilling parameters available.</p>
        </c:if>

        <a href="index.jsp" class="btn">Back to Home</a>
    </div>
</body>
</html>