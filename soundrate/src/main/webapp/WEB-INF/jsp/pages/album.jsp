<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="i18n/strings"/>
<c:set var="context" value="${pageContext.request.contextPath}"/>
<c:set var="album" value="${requestScope.album}"/>
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
    <script src="${context}/content/javascript/backlog.js"></script>
    <script src="${context}/content/javascript/sticky.js"></script>
    <script src="${context}/content/javascript/review.js"></script>
    <title>${not empty album ? album.title : ""}</title>
    <style>
        .invisible {
            display: none !important
        }
    </style>
</head>
<body>
    <c:import url="/header"/>
    <c:if test="${empty sessionScope.username}">
        <c:import url="/sign-in-modal"/>
    </c:if>
    <div class="ui container">
        <c:choose>
            <c:when test="${empty album}">
                <div class="ui placeholder segment">
                    <div class="ui large icon header">
                        <i class="circular exclamation red icon"></i>
                        <fmt:message key="error.nothingHere"/>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="ui two columns stackable grid">
                    <div class="five wide column">
                        <div class="ui sticky fluid card" data-type="album"
                             data-enabled="${empty sessionScope.username ? false : true}" data-album="${album.id}">
                            <div class="image">
                                <img src="${album.bigCover}" alt="artwork">
                            </div>
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
                                        <c:when test="${album.numberOfReviews eq 0}">
                                            <span class="ui blue circular medium label">N/A</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="ui blue circular medium label">
                                                <fmt:formatNumber type="number" maxFractionDigits="1"
                                                                  value="${album.averageRating}"/>
                                            </span>
                                            (<fmt:message key="label.basedOn">
                                                <fmt:param value="${album.numberOfReviews}"/>
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
                                <c:if test="${not empty album.genre}">
                                    <div class="center aligned meta">
                                        <span class="ui icon medium label">
                                            <i class="blue play icon"></i>
                                            <fmt:message key="label.genre"/>
                                        </span>
                                        ${album.genre}
                                    </div>
                                </c:if>
                            </div>
                            <c:choose>
                                <c:when test="${empty sessionScope.username}">
                                    <div class="ui bottom attached disabled button">
                                        <i class="sign in icon"></i> <fmt:message key="label.signInForBacklog"/>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="ui bottom attached button" data-backlog></div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <div class="eleven wide column">
                        <div class="ui fluid segment">
                            <div class="ui large blue header"><fmt:message key="label.reviews"/></div>
                            <c:if test="${not empty sessionScope.username}">
                                <div class="ui tiny basic modal" id="delete-review-modal">
                                    <div class="ui icon header">
                                        <i class="remove icon"></i>
                                    </div>
                                    <div class="content">
                                        <p><fmt:message key="tooltip.deleteReviewConfirmMessage"/></p>
                                    </div>
                                    <div class="actions">
                                        <div class="ui green basic ok inverted button">
                                            <i class="checkmark icon"></i>
                                            <fmt:message key="tooltip.cancel"/>
                                        </div>
                                        <div class="ui red basic cancel inverted button" data-delete-review>
                                            <i class="remove icon"></i>
                                            <fmt:message key="tooltip.delete"/>
                                        </div>
                                    </div>
                                </div>
                                <c:forEach items="${album.reviews}" var="review">
                                    <c:if test="${review.reviewerUsername eq sessionScope.username}">
                                        <c:set var="userReview" value="${review}"/>
                                    </c:if>
                                </c:forEach>
                                <form class="ui form ${not empty userReview ? 'invisible' : ''}" id="review-form">
                                    <div class="required field">
                                        <label><fmt:message key="tooltip.publishReview"/></label>
                                        <textarea name="content" id="review-content">${not empty userReview ? userReview.content : ""}</textarea>
                                    </div>
                                    <div class="required field">
                                        <label><fmt:message key="tooltip.rating"/></label>
                                        <div class="ui blue rating" data-icon="star"
                                             data-rating="${not empty userReview ? userReview.rating : 5}"
                                             data-max-rating="10" id="review-rating"></div>
                                    </div>
                                    <button class="ui positive small basic labeled icon button" type="submit">
                                        <i class="plus icon"></i> <fmt:message key="tooltip.publish"/>
                                    </button>
                                </form>
                                <div class="ui fluid blue card ${empty userReview ? 'invisible' : ''}" data-type="review"
                                     data-published="${not empty userReview}" data-vote-enabled="true"
                                     data-reviewer="${sessionScope.username}" data-album="${param.id}" id="user-review">
                                    <div class="meta content">
                                        <div class="right floated meta">
                                            <span class="ui icon label">
                                                <i class="blue calendar outline icon"></i>
                                                <c:choose>
                                                    <c:when test="${empty userReview}">
                                                        <jsp:useBean id="now" class="java.util.Date"/>
                                                        <c:set var="reviewPublicationDate" value="${now}"/>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:set var="reviewPublicationDate"
                                                           value="${userReview.publicationDate}"/>
                                                    </c:otherwise>
                                                </c:choose>
                                                <fmt:formatDate dateStyle="short" type="date"
                                                                value="${reviewPublicationDate}"/>
                                            </span>
                                            <span class="ui blue circular medium label"
                                                  data-user-review-rating>
                                                ${not empty userReview ? userReview.rating : ""}
                                            </span>
                                        </div>
                                    </div>
                                    <div class="content">
                                        <p data-user-review-content>${not empty userReview ? userReview.content : ""}</p>
                                    </div>
                                    <div class="extra content">
                                        <button class="ui positive small basic labeled icon button"
                                                name="edit-review-button">
                                            <i class="edit icon"></i> <fmt:message key="tooltip.edit"/>
                                        </button>
                                        <button class="ui negative small basic labeled icon button"
                                                name="delete-review-button">
                                            <i class="remove icon"></i> <fmt:message key="tooltip.delete"/>
                                        </button>
                                    </div>
                                    <div class="bottom attached button">
                                        <div class="ui fluid buttons">
                                            <button class="ui basic icon button" data-value="true">
                                                <i class="thumbs up icon"></i>
                                            </button>
                                            <div class="or"
                                                 data-text="${not empty userReview ? userReview.score : 0}"></div>
                                            <button class="ui basic icon button" data-value="false">
                                                <i class="thumbs down icon"></i>
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </c:if>
                            <c:forEach items="${album.reviews}" var="review">
                                <c:if test="${empty sessionScope.username or sessionScope.username ne review.reviewerUsername}">
                                    <div class="ui fluid card" data-type="review" data-published="true"
                                         data-vote-enabled="${not empty sessionScope.username}"
                                         data-reviewer="${review.reviewerUsername}" data-album="${param.id}">
                                        <div class="meta content">
                                            <div class="right floated meta">
                                                <span class="ui icon label">
                                                    <i class="blue calendar outline icon"></i>
                                                    <fmt:formatDate dateStyle="short" type="date"
                                                                    value="${review.publicationDate}"/>
                                                </span>
                                                <span class="ui blue circular medium label">${review.rating}</span>
                                            </div>
                                            <a class="left floated author"
                                               href="${context}/user?id=${review.reviewerUsername}">
                                                <img class="ui avatar image" src="${review.reviewer.picture}"
                                                     alt="avatar">
                                                <span class="user">${review.reviewerUsername}</span>
                                            </a>
                                        </div>
                                        <div class="content">
                                            <p>${review.content}</p>
                                        </div>
                                        <c:choose>
                                            <c:when test="${empty sessionScope.username}">
                                                <div class="bottom attached button"
                                                     data-tooltip="<fmt:message key="tooltip.signInToVote"/>">
                                                    <div class="ui fluid buttons">
                                                        <button class="ui disabled basic icon button"><i
                                                                class="thumbs up icon"></i></button>
                                                        <div class="or" data-text="${review.score}"></div>
                                                        <button class="ui disabled basic icon button"><i
                                                                class="thumbs down icon"></i></button>
                                                    </div>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="bottom attached button">
                                                    <div class="ui fluid buttons">
                                                        <button class="ui basic icon button" data-value="true"><i
                                                                class="thumbs up icon"></i></button>
                                                        <div class="or" data-text="${review.score}"></div>
                                                        <button class="ui basic icon button" data-value="false"><i
                                                                class="thumbs down icon"></i></button>
                                                    </div>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </c:if>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>