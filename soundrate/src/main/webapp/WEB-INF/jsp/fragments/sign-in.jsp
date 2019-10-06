<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="i18n/strings"/>
<c:set var="context" value="${pageContext.request.contextPath}"/>
<div class="ui tiny basic modal" id="sign-in-modal">
    <i class="close icon"></i>
    <div class="content">
        <div class="ui top attached two item tabs inverted blue menu" id="sign-in-tabs-menu">
            <a class="active item" data-tab="sign-in"><fmt:message key="label.signIn"/></a>
            <a class="item" data-tab="sign-up"><fmt:message key="label.signUp"/></a>
        </div>
        <div class="ui bottom attached active tab segment" data-tab="sign-in">
            <form class="ui form" id="sign-in-form">
                <div class="required field">
                    <label><fmt:message key="label.usernameField"/></label>
                    <div class="ui left icon input">
                        <i class="user icon"></i>
                        <input type="text" name="username" placeholder="<fmt:message key="label.usernameField"/>"
                               id="sign-in-username">
                    </div>
                </div>
                <div class="required field">
                    <label><fmt:message key="label.passwordField"/></label>
                    <div class="ui left icon input">
                        <i class="lock icon"></i>
                        <input type="password" name="password" placeholder="<fmt:message key="label.passwordField"/>"
                               id="sign-in-password">
                    </div>
                </div>
                <button class="ui primary fluid button" type="submit"><fmt:message key="label.signIn"/></button>
                <div class="ui error message"></div>
            </form>
        </div>
        <div class="ui bottom attached tab segment" data-tab="sign-up">
            <form class="ui form" id="sign-up-form">
                <div class="required field">
                    <label><fmt:message key="label.usernameField"/></label>
                    <div class="ui left icon input">
                        <i class="user icon"></i>
                        <input type="text" name="username" placeholder="<fmt:message key="label.usernameField"/>"
                               id="sign-up-user">
                    </div>
                </div>
                <div class="two fields">
                    <div class="required field">
                        <label><fmt:message key="label.emailAddressField"/></label>
                        <div class="ui left icon input">
                            <i class="envelope icon"></i>
                            <input type="email" name="email" placeholder="<fmt:message key="label.emailAddressField"/>"
                                   id="sign-up-email">
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
                <button class="ui primary fluid button" type="submit"><fmt:message key="label.signUp"/></button>
                <div class="ui error message"></div>
            </form>
        </div>
    </div>
</div>
