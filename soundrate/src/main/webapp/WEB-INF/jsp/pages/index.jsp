<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="i18n/strings/strings"/>
<c:set var="context" value="${pageContext.request.contextPath}"/>
<c:set var="sessionUser" value="${sessionScope.user}"/>
<c:set var="albums" value="${requestScope.albums}"/>
<c:set var="reviews" value="${requestScope.reviews}"/>
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
    <script src="${context}/content/javascript/vote-review.js"></script>
    <script src="${context}/content/javascript/report-review.js"></script>
    <title>soundrate</title>
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
        <div class="ui two column stackable grid">
            <div class="ten wide column">
                <!-- @todo display placeholder or message if empty -->
                <c:if test="${not empty albums}">
                    <c:set var="albumNumberOfReviewsMap" value="${requestScope.albumNumberOfReviewsMap}"/>
                    <c:set var="albumAverageRatingMap" value="${requestScope.albumAverageRatingMap}"/>
                    <div class="ui segment">
                        <div class="ui large blue header">
                            <fmt:message key="label.topAlbums"/>
                        </div>
                        <div class="ui four doubling cards">
                            <c:forEach items="${albums}" var="album">
                                <c:set var="albumNumberOfReviews" value="${albumNumberOfReviewsMap[album]}"/>
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
                                               href="${context}/artist?id=${album.artist.id}">
                                                    ${album.artist.name}
                                            </a>
                                        </div>
                                    </div>
                                    <div class="extra content">
                                        <div class="center aligned meta">
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
                </c:if>
            </div>
            <div class="six wide column">
                <!-- @todo display placeholder or message if empty -->
                <c:if test="${not empty reviews}">
                    <c:set var="reviewerMap" value="${requestScope.reviewerMap}"/>
                    <c:set var="reviewedAlbumMap" value="${requestScope.reviewedAlbumMap}"/>
                    <c:set var="reviewScoreMap" value="${requestScope.reviewScoreMap}"/>
                    <div class="ui fluid segment">
                        <div class="ui large blue header">
                            <fmt:message key="label.topReviews"/>
                        </div>
                        <c:forEach items="${reviews}" var="review">
                            <c:set var="reviewer" value="${reviewerMap[review]}"/>
                            <c:set var="reviewedAlbum" value="${reviewedAlbumMap[review]}"/>
                            <c:set var="reviewedAlbumArtist" value="${reviewedAlbum.artist}"/>
                            <c:set var="reviewScore" value="${reviewScoreMap[review]}"/>
                            <div class="ui fluid card" data-type="review" data-published="true"
                                 data-reviewer="${review.reviewer.username}" data-album="${review.reviewedAlbumId}">
                                <div class="meta content">
                                    <div class="right floated meta">
                                        <c:if test="${not empty sessionUser and review.reviewerUsername ne sessionUser.username}">
                                            <button class="ui tiny inverted icon button"
                                                    data-tooltip="<fmt:message key="tooltip.report"/>"
                                                    data-report>
                                                <i class="red flag icon"></i>
                                            </button>
                                        </c:if>
                                        <button class="ui tiny inverted icon button">
                                            <a href="${context}/review?reviewer=${review.reviewer.username}&album=${review.reviewedAlbumId}">
                                                <i class="external alternate icon"></i>
                                            </a>
                                        </button>
                                        <span class="ui icon label">
                                            <i class="blue calendar outline icon"></i>
                                            <fmt:formatDate dateStyle="short" type="date"
                                                            value="${review.publicationDate}"/>
                                        </span>
                                        <span class="ui blue circular medium label">${review.rating}</span>
                                    </div>
                                    <a class="left floated author" href="${context}/user?id=${review.reviewer.username}">
                                        <img class="ui avatar image" src="${reviewer.picture}" alt="avatar">
                                        <span class="user">${review.reviewer.username}</span>
                                    </a>
                                </div>
                                <div class="meta content">
                                    <div class="ui items">
                                        <div class="ui item">
                                            <a class="ui tiny image" href="${context}/album?id=${review.reviewedAlbumId}">
                                                <img src="${reviewedAlbum.bigCover}" alt="artwork">
                                            </a>
                                            <div class="middle aligned content">
                                                <div class="center aligned meta">
                                                    <a class="ui small header"
                                                       href="${context}/album?id=${review.reviewedAlbumId}">
                                                            ${reviewedAlbum.title}
                                                    </a>
                                                </div>
                                                <div class="center aligned meta">
                                                    <a class="ui small disabled header"
                                                       href="${context}/artist?id=${reviewedAlbumArtist.id}">
                                                            ${reviewedAlbumArtist.name}
                                                    </a>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="content">
                                    <p>${fn:escapeXml(review.content)}</p>
                                </div>
                                <c:choose>
                                    <c:when test="${empty sessionUser}">
                                        <div class="bottom attached button"
                                             data-tooltip="<fmt:message key="tooltip.logInToVote"/>">
                                            <div class="ui fluid buttons">
                                                <button class="ui disabled basic icon button">
                                                    <i class="thumbs up icon"></i>
                                                </button>
                                                <div class="or" data-text="${reviewScore}"></div>
                                                <button class="ui disabled basic icon button">
                                                    <i class="thumbs down icon"></i>
                                                </button>
                                            </div>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="bottom attached button">
                                            <div class="ui fluid buttons">
                                                <button class="ui basic icon button" data-value="true">
                                                    <i class="thumbs up icon"></i>
                                                </button>
                                                <div class="or" data-text="${reviewScore}"></div>
                                                <button class="ui basic icon button" data-value="false">
                                                    <i class="thumbs down icon"></i>
                                                </button>
                                            </div>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:forEach>
                    </div>
                </c:if>
            </div>
        </div>
    </div>
</body>
</html>
