<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="i18n/strings/strings"/>
<c:set var="context" value="${pageContext.request.contextPath}"/>
<div class="ui tiny basic modal" id="user-settings-modal">
    <i class="close icon"></i>
    <div class="content">
        <div class="ui top attached two item tabs inverted blue menu" id="user-settings-tabs-menu">
            <a class="active item" data-tab="update-email-form"><fmt:message key="label.updateEmail"/></a>
            <a class="item" data-tab="update-password-form"><fmt:message key="label.updatePassword"/></a>
        </div>
        <div class="ui bottom attached active tab segment" data-tab="update-email-form">
            <form class="ui form" id="update-email-form" onsubmit="return false">
                <div class="two fields">
                    <div class="required field">
                        <label><fmt:message key="label.newEmailAddressField"/></label>
                        <div class="ui left icon input">
                            <i class="envelope icon"></i>
                            <input type="email" name="new-email"
                                   placeholder="<fmt:message key="label.newEmailAddressField"/>"
                                   id="update-email-new-email">
                        </div>
                    </div>
                    <div class="required field">
                        <label><fmt:message key="label.matchEmailAddressField"/></label>
                        <div class="ui left icon input">
                            <i class="envelope icon"></i>
                            <input type="email" name="new-email-match"
                                   placeholder="<fmt:message key="label.matchEmailAddressField"/>"
                                   id="update-email-new-email-match">
                        </div>
                    </div>
                </div>
                <div class="required field">
                    <label><fmt:message key="label.passwordField"/></label>
                    <div class="ui left icon input">
                        <i class="lock icon"></i>
                        <input type="password" name="current-password"
                               placeholder="<fmt:message key="label.passwordField"/>"
                               id="update-email-current-password">
                    </div>
                </div>
                <button class="ui primary fluid button" type="button" id="update-email-button">
                    <fmt:message key="label.updateEmail"/>
                </button>
                <div class="ui success message">
                    <p><fmt:message key="message.emailUpdatedSuccess"/></p>
                </div>
                <div class="ui error message"></div>
            </form>
        </div>
        <div class="ui bottom attached tab segment" data-tab="update-password-form">
            <form class="ui form" id="update-password-form" onsubmit="return false">
                <div class="two fields">
                    <div class="required field">
                        <label><fmt:message key="label.newPasswordField"/></label>
                        <div class="ui left icon input">
                            <i class="lock icon"></i>
                            <input type="password" name="new-password"
                                   placeholder="<fmt:message key="label.newPasswordField"/>"
                                   id="update-password-new-password">
                        </div>
                    </div>
                    <div class="required field">
                        <label><fmt:message key="label.matchPasswordField"/></label>
                        <div class="ui left icon input">
                            <i class="lock icon"></i>
                            <input type="password" name="new-password-match"
                                   placeholder="<fmt:message key="label.matchPasswordField"/>"
                                   id="update-password-new-password-match">
                        </div>
                    </div>
                </div>
                <div class="required field">
                    <label><fmt:message key="label.currentPasswordField"/></label>
                    <div class="ui left icon input">
                        <i class="lock icon"></i>
                        <input type="password" name="current-password"
                               placeholder="<fmt:message key="label.currentPasswordField"/>"
                               id="update-password-current-password">
                    </div>
                </div>
                <button class="ui primary fluid button" type="button" id="update-password-button">
                    <fmt:message key="label.updatePassword"/>
                </button>
                <div class="ui success message">
                    <p><fmt:message key="message.passwordUpdatedSuccess"/></p>
                </div>
                <div class="ui error message"></div>
            </form>
        </div>
    </div>
</div>