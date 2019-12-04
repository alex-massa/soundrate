<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="i18n/strings/strings"/>
<c:set var="context" value="${pageContext.request.contextPath}"/>
<c:set var="sessionUser" value="${sessionScope.user}"/>
<c:set var="isModerator" value="${empty sessionUser ? null : requestScope.isModerator}"/>
<div class="ui stackable secondary inverted blue menu">
    <a class="item" href="${context}/index">
        <img class="ui middle aligned mini image" src="${context}/content/images/logo.svg" alt="logo">
    </a>
    <a class="item" href="${context}/top">
        <fmt:message key="label.topAlbums"/>
    </a>
    <div class="right menu">
        <c:choose>
            <c:when test="${empty sessionUser}">
                <div class="item">
                    <div class="ui inverted button" id="sign-in-button">
                        <i class="sign out alternate icon"></i>
                        <fmt:message key="label.signIn"/>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <a class="item" href="${context}/user?id=${sessionUser.username}" data-user="${sessionUser.username}">
                    <img class="ui avatar image" src="${sessionUser.picture}" alt="avatar">
                    ${sessionUser.username}
                </a>
                <a class="item" href="${context}/backlog?id=${sessionUser.username}">
                    <i class="list icon"></i>
                    <fmt:message key="label.backlog"/>
                </a>
                <c:if test="${not empty sessionUser and isModerator}">
                    <a class="item" href="${context}/reports-manager">
                        <i class="flag icon"></i>
                        <fmt:message key="label.reports"/>
                    </a>
                </c:if>
                <div class ="item">
                    <div class="ui inverted button" id="user-settings-button">
                        <i class="cogs icon"></i>
                        <fmt:message key="label.userSettings"/>
                    </div>
                </div>
                <div class="item">
                    <div class="ui inverted button" id="log-out-button">
                        <i class="sign out alternate icon"></i>
                        <fmt:message key="label.logOut"/>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
        <div class="ui item">
            <form class="ui form" id="search-bar">
                <div class="field">
                    <div class="ui action input">
                        <input type="text" name="query" placeholder="<fmt:message key="label.search"/>">
                        <button class="ui icon button" type="submit">
                            <i class="blue search link icon"></i>
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>
