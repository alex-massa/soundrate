<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="i18n/strings/strings"/>
<c:set var="context" value="${pageContext.request.contextPath}"/>
<c:set var="sessionUser" value="${sessionScope.user}"/>
<c:set var="isModerator" value="${empty sessionUser ? null : requestScope.isModerator}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="theme-color" content="#2962FF">
    <link rel="icon" href="${context}/favicon.ico">
    <link rel="stylesheet" type="text/css" href="https://cdn.jsdelivr.net/npm/fomantic-ui@2.8.2/dist/semantic.min.css">
    <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/v/se/dt-1.10.20/sl-1.3.1/datatables.min.css"/>
    <script src="https://code.jquery.com/jquery-3.4.1.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/fomantic-ui@2.8.2/dist/semantic.min.js"></script>
    <script type="text/javascript" src="https://cdn.datatables.net/v/se/dt-1.10.20/sl-1.3.1/datatables.min.js"></script>
    <script src="${context}/content/javascript/toast.js"></script>
    <script src="${context}/content/javascript/sign-user.js"></script>
    <script src="${context}/content/javascript/search.js"></script>
    <script src="${context}/content/javascript/user-settings.js"></script>
    <script src="${context}/content/javascript/reports-manager.js"></script>
    <title><fmt:message key="label.reports"/></title>
</head>
<body>
    <c:import url="/header"/>
    <c:choose>
        <c:when test="${empty sessionUser}">
            <c:import url="/sign-in-modal"/>
        </c:when>
        <c:otherwise>
            <c:import url="/user-settings-modal"/>
        </c:otherwise>
    </c:choose>
    <div class="ui container">
        <c:choose>
            <c:when test="${empty sessionUser or not isModerator}">
                <div class="ui placeholder segment">
                    <div class="ui large icon header">
                        <i class="ui circular red exclamation icon"></i>
                        <fmt:message key="error.unauthourized"/>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <table id="reports-table" class="ui celled table">
                    <thead>
                        <tr>
                            <th></th>
                            <th>Reviewer</th>
                            <th>Reviewed Album ID</th>
                            <th>Rating</th>
                            <th>Publication Date</th>
                        </tr>
                    </thead>
                    <tbody></tbody>
                </table>
                <div class="ui divider"></div>
                <button class="ui blue button" name="delete-review-button">
                    <fmt:message key="label.deleteReview"/>
                </button>
                <button class="ui blue button" name="delete-reports-button">
                    <fmt:message key="label.deleteReports"/>
                </button>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>
