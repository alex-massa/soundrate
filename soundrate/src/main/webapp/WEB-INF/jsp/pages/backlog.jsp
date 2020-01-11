<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="i18n/strings/strings"/>
<c:set var="context" value="${pageContext.request.contextPath}"/>
<c:set var="sessionUser" value="${sessionScope.user}"/>
<c:set var="user" value="${requestScope.user}"/>
<c:set var="backlogAlbums" value="${requestScope.backlogAlbums}"/>
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
    <script src="${context}/content/javascript/backlog.js"></script>
    <%-- @fixme --%>
    <title>${not empty user ? user.username.concat("\'s backlog") : ""}</title>
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
            <c:when test="${empty user}">
                <div class="ui placeholder segment">
                    <div class="ui large icon header">
                        <i class="ui circular red exclamation icon"></i>
                        <fmt:message key="error.nothingHere"/>
                    </div>
                </div>
            </c:when>
            <c:when test="${empty backlogAlbums}">
                <div class="ui placeholder segment">
                    <div class="ui large icon header">
                        <i class="ui circular blue exclamation icon"></i>
                        <fmt:message key="label.emptyBacklog"/>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <c:set var="albumGenreMap" value="${requestScope.albumGenreMap}"/>
                <c:set var="albumNumberOfReviewsMap" value="${requestScope.albumNumberOfReviewsMap}"/>
                <c:set var="albumAverageRatingMap" value="${requestScope.albumAverageRatingMap}"/>
                <div class="ui fluid segment">
                    <div class="ui four doubling cards">
                        <c:forEach items="${backlogAlbums}" var="album">
                            <c:set var="albumGenre" value="${albumGenreMap[album]}"/>
                            <c:set var="albumNumberOfReviews" value="${albumNumberOfReviewsMap[album]}"/>
                            <c:set var="albumAverageRating" value="${albumAverageRatingMap[album]}"/>
                            <div class="ui card" data-type="album" data-album="${album.id}">
                                <a class="image" href="${context}/album?id=${album.id}">
                                    <img src="${album.bigCover}" alt="artwork">
                                </a>
                                <div class="content">
                                    <div class="center aligned meta">
                                        <a class="ui small header"
                                           href="${context}/album?id=${album.id}">
                                                ${album.title}
                                        </a>
                                    </div>
                                    <div class="center aligned meta">
                                        <a class="ui small disabled header"
                                           href="${context}/artist?id=${album.artist.id}">
                                                ${album.artist.name}
                                        </a>
                                    </div>
                                </div>
                                <div class="extra content">
                                    <div class="center aligned meta">
                                        <span class="ui icon medium label">
                                            <i class="blue sort numeric up icon"></i>
                                            <fmt:message key="label.averageRating"/>
                                        </span>
                                        <c:choose>
                                            <c:when test="${albumNumberOfReviews eq 0}">
                                                <span class="ui blue circular medium label">N/A</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="ui blue circular medium label">
                                                    <fmt:formatNumber type="number" maxFractionDigits="1"
                                                                      value="${albumAverageRating}"/>
                                                </span>
                                                (<fmt:message key="label.basedOn">
                                                    <fmt:param value="${albumNumberOfReviews}"/>
                                                </fmt:message>)
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="center aligned meta">
                                        <span class="ui icon medium label">
                                            <i class="blue calendar outline icon"></i>
                                            <fmt:message key="label.releaseDate"/>
                                        </span>
                                        <fmt:formatDate dateStyle="short" type="date" value="${album.releaseDate}"/>
                                    </div>
                                    <c:if test="${not empty albumGenre}">
                                        <div class="center aligned meta">
                                            <span class="ui icon medium label">
                                                <i class="blue play icon"></i>
                                                <fmt:message key="label.genre"/>
                                            </span>
                                            ${albumGenre.name}
                                        </div>
                                    </c:if>
                                </div>
                                <c:choose>
                                    <c:when test="${empty sessionUser}">
                                        <div class="ui bottom attached disabled button">
                                            <i class="sign in icon"></i>
                                            <fmt:message key="label.logInForBacklog"/>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="ui bottom attached button" data-backlog></div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>
