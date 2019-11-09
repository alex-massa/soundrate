window.addEventListener('load', () => {

    let resetPasswordForm = document.getElementById('reset-password-form');
    let resetPasswordButton = document.getElementById('reset-button');
    if (resetPasswordButton)
        resetPasswordButton.addEventListener('click', () => resetPassword());

    function resetPassword() {
        let token = resetPasswordForm.dataset.token;
        let password = $(resetPasswordForm).form('get value', 'password');
        $.ajax({
            method: 'POST',
            url: 'reset-password',
            data: {token: token, password: password},
            beforeSend: xhr => {
                resetPasswordButton.disabled = true;
                if (!$(resetPasswordForm).form('is valid')) {
                    $(resetPasswordForm).form('validate form');
                    xhr.abort();
                }
            }
        })
        .done(() => $(resetPasswordForm).form('validate form'))
        .fail(xhr => {
            resetPasswordButton.disabled = false;
            if (xhr.statusText === 'canceled')
                return;
            let errorMessage = xhr.responseText || 'An unknown error occurred, please try again';
            $(resetPasswordForm).form('add errors', [errorMessage]);
        });
    }

    $(resetPasswordForm).form({
        on: 'blur',
        fields: {
            password: {
                identifier: 'password',
                rules: [{
                    type: 'regExp[/^(?=(.*\\d){2})[0-9a-zA-Z]{8,72}$/]',
                    prompt: 'The password must be composed of 8-72 alphanumeric characters and contain at least 2 digits'
                }]
            },
            passwordMatch: {
                identifier: 'password-match',
                rules: [{
                    type: 'match[reset-password]',
                    prompt: 'The provided passwords do not match'
                }]
            }
        }
    })

});
