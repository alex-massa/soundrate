window.addEventListener('load', () => {
    let recoverAccountForm = document.getElementById('recover-account-form');
    let recoverAccountButton = document.getElementById('recover-account-button');
    if (recoverAccountButton)
        recoverAccountButton.addEventListener('click', () => recoverAccount());

    function recoverAccount() {
        let email = $(recoverAccountForm).form('get value', 'email');
        $.ajax({
            method: 'post',
            url: 'recover-user-account',
            data: {email: email},
            beforeSend: xhr => {
                recoverAccountButton.disabled = true;
                if (!$(recoverAccountForm).form('is valid')) {
                    $(recoverAccountForm).form('validate form');
                    xhr.abort();
                }
            }
        })
        .done(() => $(recoverAccountForm).form('validate form'))
        .fail(xhr => {
            recoverAccountButton.disabled = false;
            if (xhr.statusText === 'canceled')
                return;
            let errorMessage = xhr.responseText || 'An unknown error occurred, please try again';
            $(recoverAccountForm).form('add errors', [errorMessage]);
        });
    }

    $(recoverAccountForm).form({
        on: 'blur',
        fields: {
            email: {
                identifier: 'email',
                rules: [{
                    type: 'email',
                    prompt: 'Insert a valid e-mail address'
                }]
            }
        }
    })
});
