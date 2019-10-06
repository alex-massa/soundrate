<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="i18n/strings"/>
<c:set var="context" value="${pageContext.request.contextPath}"/>
<c:set var="artist" value="${requestScope.artist}"/>
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
    <title>${not empty artist ? artist.name : ""}</title>
</head>
<body>
    <c:import url="/header"/>
    <c:if test="${empty sessionScope.username}">
        <c:import url="/sign-in-modal"/>
    </c:if>
    <div class="ui container">
        <c:choose>
            <c:when test="${empty artist}">
                <div class="ui placeholder segment">
                    <div class="ui large icon header">
                        <i class="circular exclamation red icon"></i>
                        <fmt:message key="error.nothingHere"/>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
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
                                    <c:when test="${artist.numberOfReviews eq 0}">
                                        <span class="ui blue circular medium label">N/A</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="ui blue circular medium label">
                                            <fmt:formatNumber type="number" maxFractionDigits="1"
                                                              value="${artist.averageRating}"/>
                                        </span>
                                        (<fmt:message key="label.basedOn">
                                            <fmt:param value="${artist.numberOfReviews}"/>
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
                <div class="ui six doubling cards">
                    <c:forEach items="${artist.albums}" var="album">
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
                                       href="${context}/artist?id=${artist.id}">${artist.name}</a>
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
</body>
</html>
