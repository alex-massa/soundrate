window.addEventListener('load', () => {

    let user = document.querySelector('[data-user]');

    $('#user-settings-tabs-menu .item').tab();

    let updateEmailForm = document.getElementById('update-email-form');
    let updatePasswordForm = document.getElementById('update-password-form');

    let userSettingsButton = document.getElementById('user-settings-button');
    if (userSettingsButton) {
        userSettingsButton.addEventListener('click', () => {
           let userSettingsModal = document.getElementById('user-settings-modal');
           $(userSettingsModal).modal({autofocus: false}).modal('show');
        });
    }

    let updateEmailButton = document.getElementById('update-email-button');
    if (updateEmailButton)
        updateEmailButton.addEventListener('click', () => updateEmail());

    let updatePasswordButton = document.getElementById('update-password-button');
    if (updatePasswordButton)
        updatePasswordButton.addEventListener('click', () => updatePassword());

    function updateEmail() {
        let username = user.dataset.user;
        let newEmail = $(updateEmailForm).form('get value', 'new-email');
        let currentPassword = $(updateEmailForm).form('get value', 'current-password');
        $.ajax({
            method: 'post',
            url: 'update-email',
            data: {username: username, cpassword: currentPassword, nemail: newEmail},
            beforeSend: xhr => {
                updateEmailButton.disabled = true;
                if (!$(updateEmailForm).form('is valid')) {
                    $(updateEmailForm).form('validate form');
                    xhr.abort();
                }
            }
        })
        .done(() => $(updateEmailForm).form('validate form'))
        .fail(xhr => {
            if (xhr.statusText === 'canceled')
                return;
            let errorMessage = xhr.responseText || 'An unknown error occurred, please try again';
            $(updateEmailForm).form('add errors', [errorMessage]);
        })
        .always(() => updateEmailButton.disabled = false);
    }

    function updatePassword() {
        let username = user.dataset.user;
        let newPassword = $(updatePasswordForm).form('get value', 'new-password');
        let currentPassword = $(updatePasswordForm).form('get value', 'current-password');
        $.ajax({
            method: 'post',
            url: 'update-password',
            data: {username: username, cpassword: currentPassword, npassword: newPassword},
            beforeSend: xhr => {
                updatePasswordButton.disabled = true;
                if (!$(updatePasswordForm).form('is valid')) {
                    $(updatePasswordForm).form('validate form');
                    xhr.abort();
                }
            }
        })
        .done(() => $(updatePasswordForm).form('validate form'))
        .fail(xhr => {
            if (xhr.statusText === 'canceled')
                return;
            let errorMessage = xhr.responseText || 'An unknown error occurred, please try again';
            $(updatePasswordForm).form('add errors', [errorMessage]);
        })
        .always(() => updatePasswordButton.disabled = false);
    }

    $(updateEmailForm).form({
        on: 'blur',
        fields: {
            newEmail: {
                identifier: 'new-email',
                rules: [{
                    type: 'email',
                    prompt: 'Insert a valid e-mail address'
                }]
            },
            newEmailMatch: {
                identifier: 'new-email-match',
                rules: [{
                    type: 'match[update-email-new-email]',
                    prompt: 'The provided e-mail addresses do not match'
                }]
            },
            currentPassword: {
                identifier: 'current-password',
                rules: [{
                    type: 'empty',
                    prompt: 'Insert a password'
                }]
            }
        }
    });

    $(updatePasswordForm).form({
       on: 'blur',
       fields: {
           newPassword: {
               identifier: 'new-password',
               rules: [{
                   type: 'regExp[/^(?=(.*\\d){2})[0-9a-zA-Z]{8,72}$/]',
                   prompt: 'The password must be composed of 8-72 alphanumeric characters and contain at least 2 digits'
               }]
           },
           newPasswordMatch: {
               identifier: 'new-password-match',
               rules: [{
                   type: 'match[update-password-new-password]',
                   prompt: 'The provided passwords do not match'
               }]
           },
           currentPassword: {
               identifier: 'current-password',
               rules: [{
                   type: 'empty',
                   prompt: 'Insert a password'
               }]
           }
       }
    });

});
