<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="i18n/strings/strings"/>
<c:set var="context" value="${pageContext.request.contextPath}"/>
<c:set var="sessionUser" value="${sessionScope.user}"/>
<c:set var="artist" value="${requestScope.artist}"/>
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
    <title>${not empty artist ? artist.name : ""}</title>
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
            <c:when test="${empty artist}">
                <div class="ui placeholder segment">
                    <div class="ui large icon header">
                        <i class="ui circular red exclamation icon"></i>
                        <fmt:message key="error.nothingHere"/>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <c:set var="artistAlbums" value="${requestScope.artistAlbums}"/>
                <c:set var="artistReviewsCount" value="${requestScope.artistReviewsCount}"/>
                <c:set var="artistAverageRating" value="${requestScope.artistAverageRating}"/>
                <div class="ui items">
                    <div class="item">
                        <div class="ui small circular image">
                            <img src="${artist.bigPicture}" alt="picture">
                        </div>
                        <div class="content">
                            <div class="header">
                                <span class="ui medium blue header">${artist.name}</span>
                            </div>
                            <div class="meta">
                                <span class="ui icon medium label">
                                    <i class="blue sort numeric up icon"></i>
                                    <fmt:message key="label.averageRating"/>
                                </span>
                                <c:choose>
                                    <c:when test="${artistReviewsCount eq 0}">
                                        <span class="ui blue circular medium label">N/A</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="ui blue circular medium label">
                                            <fmt:formatNumber type="number" maxFractionDigits="1"
                                                              value="${artistAverageRating}"/>
                                        </span>
                                        (<fmt:message key="label.basedOn">
                                            <fmt:param value="${artistReviewsCount}"/>
                                        </fmt:message>)
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="ui large blue header">
                    <fmt:message key="label.albums"/>
                </div>
                <c:choose>
                    <c:when test="${empty artistAlbums}">
                        <div class="ui placeholder segment">
                            <div class="ui large icon header">
                                <i class="ui circular blue exclamation icon"></i>
                                <fmt:message key="label.noAvailableAlbums"/>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="ui six doubling cards">
                            <c:set var="albumReviewsCountMap" value="${requestScope.albumReviewsCountMap}"/>
                            <c:set var="albumAverageRatingMap" value="${requestScope.albumAverageRatingMap}"/>
                            <c:forEach items="${artistAlbums}" var="album">
                                <c:set var="albumReviewsCount" value="${albumReviewsCountMap[album]}"/>
                                <c:set var="albumAverageRating" value="${albumAverageRatingMap[album]}"/>
                                <div class="card">
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
                                               href="${context}/artist?id=${artist.id}">
                                                    ${artist.name}
                                            </a>
                                        </div>
                                    </div>
                                    <div class="extra content">
                                        <div class="center aligned meta">
                                            <c:choose>
                                                <c:when test="${albumReviewsCount eq 0}">
                                                    <span class="ui blue circular medium label">N/A</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="ui blue circular medium label">
                                                        <fmt:formatNumber type="number" maxFractionDigits="1"
                                                                          value="${albumAverageRating}"/>
                                                    </span>
                                                    <span>
                                                        (<fmt:message key="label.reviewsCount">
                                                            <fmt:param value="${albumReviewsCount}"/>
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
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>
