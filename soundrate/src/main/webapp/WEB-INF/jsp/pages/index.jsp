<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="i18n/strings"/>
<c:set var="context" value="${pageContext.request.contextPath}"/>
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
    <link rel="stylesheet" type="text/css" href="${context}/content/semantic/dist/semantic.min.css">
    <script src="https://code.jquery.com/jquery-3.3.1.min.js"></script>
    <script src="${context}/content/semantic/dist/semantic.min.js"></script>
    <script src="${context}/content/javascript/api-settings.js"></script>
    <script src="${context}/content/javascript/sign-user.js"></script>
    <script src="${context}/content/javascript/search.js"></script>
    <script src="${context}/content/javascript/vote-review.js"></script>
    <title>soundrate</title>
</head>
<body>
    <c:import url="/header"/>
    <c:if test="${empty sessionScope.username}">
        <c:import url="/sign-in-modal"/>
    </c:if>
    <div class="ui container">
        <div class="ui two column stackable grid">
            <div class="ten wide column">
                <div class="ui segment">
                    <div class="ui large blue header">
                        <fmt:message key="label.topAlbums"/>
                    </div>
                    <div class="ui four doubling cards">
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
                </div>
            </div>
            <div class="six wide column">
                <div class="ui fluid segment">
                    <div class="ui large blue header">
                        <fmt:message key="label.topReviews"/>
                    </div>
                    <c:forEach items="${reviews}" var="review">
                        <div class="ui fluid card" data-type="review" data-published="true"
                             data-vote-enabled="${not empty sessionScope.username}"
                             data-reviewer="${review.reviewerUsername}" data-album="${review.reviewedAlbumId}">
                            <div class="meta content">
                                <div class="right floated meta">
                                    <span class="ui icon label">
                                        <i class="blue calendar outline icon"></i>
                                        <fmt:formatDate dateStyle="short" type="date" value="${review.publicationDate}"/>
                                    </span>
                                    <span class="ui blue circular medium label">${review.rating}</span>
                                </div>
                                <a class="left floated author" href="${context}/user?id=${review.reviewerUsername}">
                                    <img class="ui avatar image" src="${review.reviewer.picture}" alt="avatar">
                                    <span class="user">${review.reviewerUsername}</span>
                                </a>
                            </div>
                            <div class="meta content">
                                <div class="ui items">
                                    <div class="ui item">
                                        <a class="ui tiny image" href="${context}/album?id=${review.reviewedAlbumId}">
                                            <img src="${review.reviewedAlbum.bigCover}" alt="artwork">
                                        </a>
                                        <div class="middle aligned content">
                                            <div class="center aligned meta">
                                                <a class="ui small header"
                                                   href="${context}/album?id=${review.reviewedAlbumId}">
                                                        ${review.reviewedAlbum.title}
                                                </a>
                                            </div>
                                            <div class="center aligned meta">
                                                <a class="ui small disabled header"
                                                   href="${context}/artist?id=${review.reviewedAlbum.artist.id}">
                                                        ${review.reviewedAlbum.artist.name}
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
                                <c:when test="${empty sessionScope.username}">
                                    <div class="bottom attached button"
                                         data-tooltip="<fmt:message key="tooltip.signInToVote"/>">
                                        <div class="ui fluid buttons">
                                            <button class="ui disabled basic icon button">
                                                <i class="thumbs up icon"></i>
                                            </button>
                                            <div class="or" data-text="${review.score}"></div>
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
                                            <div class="or" data-text="${review.score}"></div>
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
    </div>
</body>
</html>
