<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>View Letter - CHSS Portal</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
</head>
<body>
    <div class="app-container">
        <c:if test="${not empty success}">
            <div class="toast toast-success" id="toast">
                <span>${success}</span>
                <button onclick="dismissToast()" class="toast-close">&times;</button>
            </div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="toast toast-error" id="toast">
                <span>${error}</span>
                <button onclick="dismissToast()" class="toast-close">&times;</button>
            </div>
        </c:if>

        <main class="main-content">

            <div class="card">
                <div class="card-body" style="padding: 0;">
                    <div class="pdf-viewer" style="width: 100%; height: 75vh;">
                        <iframe src="${pageContext.request.contextPath}/print/${letter.letterId}"
                                style="width: 100%; height: 100%; border: none;" frameborder="0"></iframe>
                    </div>
                </div>
                <div class="actions-bar">
                    <a href="${pageContext.request.contextPath}/download/${letter.letterId}" class="btn btn-yellow">
                        Download PDF
                    </a>
                    <a href="${pageContext.request.contextPath}/print/${letter.letterId}" class="btn btn-secondary" target="_blank">
                        Print
                    </a>
                    <a href="${pageContext.request.contextPath}/edit/${letter.letterId}" class="btn btn-secondary">
                        Edit
                    </a>
                    <form method="post" action="${pageContext.request.contextPath}/regenerate/${letter.letterId}" style="display:inline;">
                        <button type="submit" class="btn btn-secondary">
                            Regenerate
                        </button>
                    </form>
                </div>
            </div>
        </main>
    </div>
    <script>
    function dismissToast() {
        var toast = document.getElementById('toast');
        if (toast) toast.remove();
    }
    </script>
</body>
</html>
