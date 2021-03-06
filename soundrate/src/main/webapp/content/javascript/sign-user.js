window.addEventListener('load', () => {
    $('#sign-in-tabs-menu .item').tab();

    let logInForm = document.getElementById('log-in-form');
    let signUpForm = document.getElementById('sign-up-form');

    let signInButton = document.getElementById('sign-in-button');
    if (signInButton) {
        signInButton.addEventListener('click', () => {
            let signInModal = document.getElementById('sign-in-modal');
            $(signInModal).modal({autofocus: false}).modal('show');
        });
    }

    let logInButton = document.getElementById('log-in-button');
    if (logInButton)
        logInButton.addEventListener('click', () => logIn());

    let signUpButton = document.getElementById('sign-up-button');
    if (signUpButton)
        signUpButton.addEventListener('click', () => signUp());

    let logOutButton = document.getElementById('log-out-button');
    if (logOutButton)
        logOutButton.addEventListener('click', () => logOut());

    function logIn() {
        let username = $(logInForm).form('get value', 'username');
        let password = $(logInForm).form('get value', 'password');
        $.ajax({
            method: 'post',
            url: 'log-in',
            data: {username: username, password: password},
            beforeSend: xhr => {
                logInButton.disabled = true;
                if (!$(logInForm).form('is valid')) {
                    $(logInForm).form('validate form');
                    xhr.abort();
                }
            }
        })
        .done(() => {
            $(logInForm).form('validate form');
            location.reload()
        })
        .fail(xhr => {
            logInButton.disabled = false;
            if (xhr.statusText === 'canceled')
                return;
            let errorMessage = xhr.responseText || 'An unknown error occurred, please try again';
            $(logInForm).form('add errors', [errorMessage]);
        });
    }

    function signUp() {
        let username = $(signUpForm).form('get value', 'username');
        let email = $(signUpForm).form('get value', 'email');
        let password = $(signUpForm).form('get value', 'password');
        $.ajax({
            method: 'post',
            url: 'sign-up',
            data: {username: username, email: email, password: password},
            beforeSend: xhr => {
                signInButton.disabled = true;
                if (!$(signUpForm).form('is valid')) {
                    $(signUpForm).form('validate form');
                    xhr.abort();
                }
            }
        })
        .done(() => {
            $(signUpForm).form('validate form');
            location.reload()
        })
        .fail(xhr => {
            signInButton.disabled = false;
            if (xhr.statusText === 'canceled')
                return;
            let errorMessage = xhr.responseText || 'An unknown error occurred, please try again';
            $(signUpForm).form('add errors', [errorMessage]);
        });
    }

    function logOut() {
        $.ajax({
            method: 'post',
            url: 'log-out'
        })
        .done(() => location.reload())
        .fail(() => showToast('An unknown error occurred, please try again', status.ERROR))
    }

    $(logInForm).form({
        on: 'blur',
        fields: {
            username: {
                identifier: 'username',
                rules: [{
                    type: 'empty',
                    prompt: 'Insert a username'
                }]
            },
            password: {
                identifier: 'password',
                rules: [{
                    type: 'empty',
                    prompt: 'Insert a password'
                }]
            }
        }
    });

    $(signUpForm).form({
        on: 'blur',
        fields: {
            username: {
                identifier: 'username',
                rules: [{
                    type: 'regExp[/^(?=.{3,36}$)[a-zA-Z0-9]+([_ -]?[a-zA-Z0-9])*$/]',
                    prompt: 'The username must be composed of 3-36 alphanumeric characters'
                }]
            },
            email: {
                identifier: 'email',
                rules: [{
                    type: 'email',
                    prompt: 'Insert a valid e-mail address'
                }]
            },
            emailMatch: {
                identifier: 'email-match',
                rules: [{
                    type: 'match[sign-up-email]',
                    prompt: 'The provided e-mail addresses do not match'
                }]
            },
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
                    type: 'match[sign-up-password]',
                    prompt: 'The provided passwords do not match'
                }]
            }
        }
    })
});
