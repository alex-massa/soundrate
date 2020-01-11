<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="i18n/strings/strings"/>
<c:set var="context" value="${pageContext.request.contextPath}"/>
<c:set var="sessionUser" value="${sessionScope.user}"/>
<c:set var="albums" value="${requestScope.albums}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="theme-color" content="#2962FF">
    <link rel="icon" href="${context}/favicon.ico">
    <link rel="stylesheet" type="text/css" href="https://cdn.jsdelivr.net/npm/fomantic-ui@2.8.3/dist/semantic.min.css">
    <script src="https://code.jquery.com/jquery-3.4.1.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/fomantic-ui@2.8.3/dist/semantic.min.js"></script>
    <script src="${context}/content/javascript/toast.js"></script>
    <script src="${context}/content/javascript/sign-user.js"></script>
    <script src="${context}/content/javascript/search.js"></script>
    <script src="${context}/content/javascript/user-settings.js"></script>
    <title><fmt:message key="label.topAlbums"/></title>
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
        <div class="ui fluid segment">
            <div class="ui large blue header">
                <fmt:message key="label.topAlbums"/>
            </div>
            <div class="ui divided list">
                <!-- @todo add placeholder or message if no albums are available -->
                <c:set var="albumNumberOfReviewsMap" value="${requestScope.albumNumberOfReviewsMap}"/>
                <c:set var="albumAverageRatingMap" value="${requestScope.albumAverageRatingMap}"/>
                <c:forEach items="${albums}" var="album">
                    <c:set var="albumNumberOfReviews" value="${albumNumberOfReviewsMap[album]}"/>
                    <c:set var="albumAverageRating" value="${albumAverageRatingMap[album]}"/>
                    <div class="item">
                        <img class="ui tiny image" src="${album.bigCover}" alt="artwork">
                        <div class="content">
                            <div class="header">
                                <a class="ui header" href="${context}/album?id=${album.id}">
                                    ${album.title}
                                </a>
                            </div>
                            <div class="meta">
                                <a href="${context}/artist?id=${album.artist.id}">
                                    ${album.artist.name}
                                </a>
                            </div>
                            <div class="extra">
                                <c:choose>
                                    <c:when test="${albumNumberOfReviews eq 0}">
                                        <span class="ui blue circular medium label">N/A</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="ui blue circular medium label">
                                            <fmt:formatNumber type="number" maxFractionDigits="1"
                                                              value="${albumAverageRating}"/>
                                        </span>
                                        <span>
                                            (<fmt:message key="label.numberOfReviews">
                                                <fmt:param value="${albumNumberOfReviews}"/>
                                            </fmt:message>)
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>
    </div>
</body>
</html>
