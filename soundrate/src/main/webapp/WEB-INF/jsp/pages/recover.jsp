<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="i18n/strings/strings"/>
<c:set var="context" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="theme-color" content="#2962FF">
    <link rel="icon" href="${context}/favicon.ico">
    <link rel="stylesheet" type="text/css" href="${context}/content/semantic/dist/semantic.min.css">
    <script src="https://code.jquery.com/jquery-3.4.1.min.js"></script>
    <script src="${context}/content/semantic/dist/semantic.min.js"></script>
    <script src="${context}/content/javascript/sign-user.js"></script>
    <script src="${context}/content/javascript/search.js"></script>
    <script src="${context}/content/javascript/recover-account.js"></script>
    <title><fmt:message key="page.recover"/></title>
</head>
<body>
    <c:import url="/header"/>
    <c:if test="${empty sessionScope.username}">
        <c:import url="/sign-in-modal"/>
    </c:if>
    <div class="ui container">
        <c:choose>
            <c:when test="${not empty sessionScope.username}">
            <div class="ui placeholder segment">
                <div class="ui large icon header">
                    <i class="ui circular exclamation red icon"></i>
                    <fmt:message key="error.cannotRecover"/>
                </div>
            </div>
            </c:when>
            <c:otherwise>
                <div class="ui one column stackable center aligned page grid">
                    <div class="ui twelve wide column">
                        <div class="ui attached info message">
                            <div class="ui header">
                                <fmt:message key="message.helpRecover"/>
                            </div>
                            <p><fmt:message key="message.recoverAccount"/></p>
                        </div>
                        <form class="ui form attached fluid segment" id="recover-account-form" onsubmit="return false">
                            <div class="required field">
                                <div class="ui left icon input">
                                    <i class="envelope icon"></i>
                                    <input type="email" name="email"
                                           placeholder="<fmt:message key="label.emailAddressField"/>" id="recover-email">
                                </div>
                            </div>
                            <button class="ui primary fluid button" type="button" id="recover-button">
                                <fmt:message key="label.submit"/>
                            </button>
                            <div class="ui success message">
                                <div class="ui header">
                                    <fmt:message key="label.success"/>
                                </div>
                                <p><fmt:message key="message.recoveryEmailSent"/></p>
                            </div>
                            <div class="ui error message"></div>
                        </form>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>
