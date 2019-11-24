<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="i18n/strings/strings"/>
<c:set var="context" value="${pageContext.request.contextPath}"/>
<c:set var="sessionUser" value="${sessionScope.user}"/>
<c:set var="user" value="${requestScope.user}"/>
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
    <script src="${context}/content/javascript/user-settings.js"></script>
    <script src="${context}/content/javascript/search.js"></script>
    <script src="${context}/content/javascript/user-settings.js"></script>
    <script src="${context}/content/javascript/vote-review.js"></script>
    <script src="${context}/content/javascript/sticky.js"></script>
    <title>${not empty user ? user.username : ""}</title>
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
                        <i class="ui circular exclamation red icon"></i>
                        <fmt:message key="error.nothingHere"/>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <c:set var="userReviews" value="${requestScope.userReviews}"/>
                <c:set var="userNumberOfReviews" value="${requestScope.userNumberOfReviews}"/>
                <c:set var="userAverageAssignedRating" value="${requestScope.userAverageAssignedRating}"/>
                <c:set var="userReputation" value="${requestScope.userReputation}"/>
                <div class="ui two columns stackable grid">
                    <div class="five wide column">
                        <div class="ui sticky fluid card">
                            <div class="image">
                                <img src="${user.picture}" alt="avatar">
                            </div>
                            <div class="content">
                                <div class="center aligned meta">
                                    <a class="ui small header"
                                       href="${context}/user?id=${user.username}">
                                            ${user.username}
                                    </a>
                                </div>
                            </div>
                            <div class="extra content">
                                <div class="center aligned meta">
                                    <span class="ui icon medium label">
                                        <i class="blue thumbs up icon"></i>
                                        <fmt:message key="label.totalRep"/>
                                    </span>
                                    <span class="ui blue circular medium label">
                                        ${userReputation}
                                    </span>
                                </div>
                                <div class="center aligned meta">
                                    <span class="ui icon medium label">
                                        <i class="blue sort numeric up icon"></i>
                                        <fmt:message key="label.averageAssignedRating"/>
                                    </span>
                                    <c:choose>
                                        <c:when test="${userNumberOfReviews eq 0}">
                                            <span class="ui blue circular medium label">N/A</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="ui blue circular medium label">
                                                <fmt:formatNumber type="number" maxFractionDigits="1"
                                                                  value="${userAverageAssignedRating}"/>
                                            </span>
                                            (<fmt:message key="label.basedOn">
                                                <fmt:param value="${userNumberOfReviews}"/>
                                            </fmt:message>)
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="center aligned meta">
                                    <span class="ui icon medium label">
                                        <i class="blue calendar outline icon"></i>
                                        <fmt:message key="label.signUpDate"/>
                                    </span>
                                    <fmt:formatDate dateStyle="short" type="date" value="${user.signUpDate}"/>
                                </div>
                            </div>
                            <a class="ui bottom attached button" href="${context}/backlog?id=${user.username}">
                                <i class="list icon"></i>
                                <fmt:message key="label.backlog"/>
                            </a>
                        </div>
                    </div>
                    <div class="eleven wide column">
                        <div class="ui fluid segment">
                            <div class="ui large blue header">
                                <fmt:message key="label.reviews"/>
                            </div>
                            <!-- @todo display placeholder or message if no reviews are available -->
                            <c:set var="reviewedAlbumMap" value="${requestScope.reviewedAlbumMap}"/>
                            <c:set var="reviewScoreMap" value="${requestScope.reviewScoreMap}"/>
                            <c:forEach items="${userReviews}" var="review">
                                <c:set var="reviewedAlbum" value="${reviewedAlbumMap[review]}"/>
                                <c:set var="reviewedAlbumArtist" value="${reviewedAlbum.artist}"/>
                                <c:set var="reviewScore" value="${reviewScoreMap[review]}"/>
                                <div class="ui fluid card" data-type="review" data-published="true"
                                     data-reviewer="${review.reviewer.username}" data-album="${review.reviewedAlbumId}">
                                    <div class="meta content">
                                        <div class="right floated meta">
                                            <span class="ui icon label">
                                                <i class="blue calendar outline icon"></i>
                                                <fmt:formatDate dateStyle="short" type="date"
                                                                value="${review.publicationDate}"/>
                                            </span>
                                            <span class="ui blue circular medium label">${review.rating}</span>
                                        </div>
                                    </div>
                                    <div class="meta content">
                                        <div class="ui items">
                                            <div class="ui item">
                                                <a class="ui tiny image"
                                                   href="${context}/album?id=${review.reviewedAlbumId}">
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
                                        <p>${review.content}</p>
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
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>
