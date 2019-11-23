<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="i18n/strings/strings"/>
<c:set var="context" value="${pageContext.request.contextPath}"/>
<div class="ui tiny basic modal" id="sign-in-modal">
    <i class="close icon"></i>
    <div class="content">
        <div class="ui top attached two item tabs inverted blue menu" id="sign-in-tabs-menu">
            <a class="active item" data-tab="log-in-form"><fmt:message key="label.logIn"/></a>
            <a class="item" data-tab="sign-up-form"><fmt:message key="label.signUp"/></a>
        </div>
        <div class="ui bottom attached active tab segment" data-tab="log-in-form">
            <form class="ui form" id="log-in-form" onsubmit="return false">
                <div class="required field">
                    <label><fmt:message key="label.usernameField"/></label>
                    <div class="ui left icon input">
                        <i class="user icon"></i>
                        <input type="text" name="username"
                               placeholder="<fmt:message key="label.usernameField"/>" id="log-in-username">
                    </div>
                </div>
                <div class="required field">
                    <label><fmt:message key="label.passwordField"/></label>
                    <div class="ui left icon input">
                        <i class="lock icon"></i>
                        <input type="password" name="password"
                               placeholder="<fmt:message key="label.passwordField"/>" id="log-in-password">
                    </div>
                </div>
                <div class="ui center aligned basic segment">
                    <a href="${context}/recover">
                        <fmt:message key="message.forgotPassword"/>
                    </a>
                </div>
                <button class="ui primary fluid button" type="button" id="log-in-button">
                    <fmt:message key="label.logIn"/>
                </button>
                <div class="ui error message"></div>
            </form>
        </div>
        <div class="ui bottom attached tab segment" data-tab="sign-up-form">
            <form class="ui form" id="sign-up-form" onsubmit="return false">
                <div class="required field">
                    <label><fmt:message key="label.usernameField"/></label>
                    <div class="ui left icon input">
                        <i class="user icon"></i>
                        <input type="text" name="username"
                               placeholder="<fmt:message key="label.usernameField"/>" id="sign-up-username">
                    </div>
                </div>
                <div class="two fields">
                    <div class="required field">
                        <label><fmt:message key="label.emailAddressField"/></label>
                        <div class="ui left icon input">
                            <i class="envelope icon"></i>
                            <input type="email" name="email"
                                   placeholder="<fmt:message key="label.emailAddressField"/>" id="sign-up-email">
                        </div>
                    </div>
                    <div class="required field">
                        <label><fmt:message key="label.matchEmailAddressField"/></label>
                        <div class="ui left icon input">
                            <i class="envelope icon"></i>
                            <input type="email" name="email-match"
                                   placeholder="<fmt:message key="label.matchEmailAddressField"/>"
                                   id="sign-up-email-match">
                        </div>
                    </div>
                </div>
                <div class="two fields">
                    <div class="required field">
                        <label><fmt:message key="label.passwordField"/></label>
                        <div class="ui left icon input">
                            <i class="lock icon"></i>
                            <input type="password" name="password"
                                   placeholder="<fmt:message key="label.passwordField"/>" id="sign-up-password">
                        </div>
                    </div>
                    <div class="required field">
                        <label><fmt:message key="label.matchPasswordField"/></label>
                        <div class="ui left icon input">
                            <i class="lock icon"></i>
                            <input type="password" name="password-match"
                                   placeholder="<fmt:message key="label.matchPasswordField"/>"
                                   id="sign-up-password-match">
                        </div>
                    </div>
                </div>
                <button class="ui primary fluid button" type="button" id="sign-up-button">
                    <fmt:message key="label.signUp"/>
                </button>
                <div class="ui error message"></div>
            </form>
        </div>
    </div>
</div>
