<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="i18n/strings"/>
<c:set var="context" value="${pageContext.request.contextPath}"/>
<c:set var="artists" value="${requestScope.artists}"/>
<c:set var="albums" value="${requestScope.albums}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="theme-color" content="#2962FF">
    <link rel="icon" href="${context}/favicon.ico">
    <link rel="stylesheet" type="text/css" href="${context}/content/semantic/dist/semantic.min.css">
    <script src="https://code.jquery.com/jquery-3.3.1.min.js"></script>
    <script src="${context}/content/semantic/dist/semantic.min.js"></script>
    <script src="${context}/content/javascript/api-settings.js"></script>
    <script src="${context}/content/javascript/sign-user.js"></script>
    <script src="${context}/content/javascript/search.js"></script>
    <title><fmt:message key="label.searchResults"/></title>
</head>
<body>
    <c:import url="/header"/>
    <c:if test="${empty sessionScope.username}">
        <c:import url="/sign-in-modal"/>
    </c:if>
    <div class="ui container">
        <div class="ui fluid segment">
            <div class="ui large blue header">
                <fmt:message key="label.artists"/>
            </div>
            <c:choose>
                <c:when test="${empty artists}">
                    <div class="ui center aligned disabled medium icon header">
                        <i class="circular search icon"></i>
                        <fmt:message key="label.noResults"/>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="ui six doubling cards">
                        <c:forEach items="${artists}" var="artist">
                            <div class="card">
                                <a class="image" href="${context}/artist?id=${artist.id}">
                                    <img src="${artist.bigPicture}" alt="picture">
                                </a>
                                <div class="content">
                                    <div class="center aligned meta">
                                        <a class="ui small header"
                                           href="${context}/artist?id=${artist.id}">${artist.name}</a>
                                    </div>
                                </div>
                                <div class="extra content">
                                    <div class="center aligned meta">
                                        <c:choose>
                                            <c:when test="${artist.numberOfReviews eq 0}">
                                                <span class="ui blue circular medium label">N/A</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="ui blue circular medium label">
                                                    <fmt:formatNumber type="number" maxFractionDigits="1"
                                                                      value="${artist.averageRating}"/>
                                                </span>
                                                <span>
                                                    (<fmt:message key="label.numberOfReviews">
                                                        <fmt:param value="${artist.reviewsCount}"/>
                                                    </fmt:message>)
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
        <div class="ui fluid segment">
            <div class="ui large blue header">
                <fmt:message key="label.albums"/>
            </div>
            <c:choose>
                <c:when test="${empty albums}">
                    <div class="ui center aligned disabled medium icon header">
                        <i class="circular search icon"></i>
                        <fmt:message key="label.noResults"/>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="ui six doubling cards">
                        <c:forEach items="${albums}" var="album">
                            <div class="card">
                                <a class="image" href="${context}/album?id=${album.id}">
                                    <img src="${album.bigCover}" alt="artwork">
                                </a>
                                <div class="content">
                                    <div class="center aligned meta">
                                        <a class="ui small header"
                                           href="${context}/album?id=${album.id}">${album.title}</a>
                                    </div>
                                    <div class="center aligned meta">
                                        <a class="ui small disabled header"
                                           href="${context}/artist?id=${album.artist.id}">${album.artist.name}</a>
                                    </div>
                                </div>
                                <div class="extra content">
                                    <div class="center aligned meta">
                                        <c:choose>
                                            <c:when test="${album.numberOfReviews eq 0}">
                                                <span class="ui blue circular medium label">N/A</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="ui blue circular medium label">
                                                    <fmt:formatNumber type="number" maxFractionDigits="1"
                                                                      value="${album.averageRating}"/>
                                                </span>
                                                <span>
                                                    (<fmt:message key="label.numberOfReviews">
                                                        <fmt:param value="${album.numberOfReviews}"/>
                                                    </fmt:message>)
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</body>
</html>
