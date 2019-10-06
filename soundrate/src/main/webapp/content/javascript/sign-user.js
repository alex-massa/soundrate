window.addEventListener('load', () => {
    let signUpForm = document.getElementById('sign-up-form');
    let signInForm = document.getElementById('sign-in-form');

    let signInButton = document.getElementById('sign-in-button');
    if (signInButton) {
        signInButton.addEventListener('click', () => {
            let signInModal = document.getElementById('sign-in-modal');
            $(signInModal).modal({autofocus: false}).modal('show')
        });
    }
    $('#sign-in-tabs-menu .item').tab();

    $(signInForm)
        .form({
            on: 'blur',
            fields: {
                username: {
                    identifier: 'username',
                    rules: [
                        {
                            type: 'empty',
                            prompt: 'Insert a username'
                        }
                    ]
                },
                password: {
                    identifier: 'password',
                    rules: [
                        {
                            type: 'empty',
                            prompt: 'Insert a password'
                        }
                    ]
                }
            }
        })
        .api({
            action: 'sign in',
            method: 'POST',
            dataType: 'text',
            beforeSend: settings => {
                settings.urlData.username = $(signInForm).form('get value', 'username');
                settings.urlData.password = $(signInForm).form('get value', 'password');
                return settings;
            },
            onSuccess: () => location.reload(),
            onError: (errorMessage, element, xhr) => {
                if (xhr.status !== 500 && xhr.responseText)
                    $(signInForm).form('add errors', [xhr.responseText]);
                else
                    $(signInForm).form('add errors', ['An unknown error occurred, please try again']);
            }
        });

    $(signUpForm)
        .form({
            on: 'blur',
            fields: {
                username: {
                    identifier: 'username',
                    rules: [
                        {
                            type: 'regExp[/^(?=.{4,32}$)[a-zA-Z0-9]+([_ -]?[a-zA-Z0-9])*$/]',
                            prompt: 'The username must be composed of 4-32 alphanumeric characters'
                        }
                    ]
                },
                email: {
                    identifier: 'email',
                    rules: [
                        {
                            type: 'email',
                            prompt: 'Insert a valid e-mail address'
                        }
                    ]
                },
                emailMatch: {
                    identifier: 'email-match',
                    rules: [
                        {
                            type: 'match[sign-up-email]',
                            prompt: 'The provided e-mail addresses do not match'
                        }
                    ]
                },
                password: {
                    identifier: 'password',
                    rules: [
                        {
                            type: 'regExp[/^(?=(.*\\d){2})[0-9a-zA-Z]{8,72}$/]',
                            prompt: 'The password must be composed of 8-72 alphanumeric characters and contain at least 2 digits'
                        }
                    ]
                },
                passwordMatch: {
                    identifier: 'password-match',
                    rules: [
                        {
                            type: 'match[sign-up-password]',
                            prompt: 'The provided passwords do not match'
                        }
                    ]
                }
            }
        })
        .api({
            action: 'sign up',
            method: 'POST',
            dataType: 'text',
            beforeSend: settings => {
                settings.urlData.username = $(signUpForm).form('get value', 'username');
                settings.urlData.email = $(signUpForm).form('get value', 'email');
                settings.urlData.password = $(signUpForm).form('get value', 'password');
                return settings;
            },
            onSuccess: () => location.reload(),
            onError: (errorMessage, element, xhr) => {
                if (xhr.status !== 500 && xhr.responseText != null)
                    $(signUpForm).form('add errors', [xhr.responseText]);
                else
                    $(signUpForm).form('add errors', ['An unknown error occurred, please try again']);
            }
        });

    $('#sign-out-button').api({
        action: 'sign out',
        method: 'POST',
        dataType: 'text',
        onSuccess: () => location.reload(),
        onError: () => {
            $('body').toast({
                message: 'An unknown error occurred, please try again',
                position: 'bottom right',
                class: 'error',
                className: {toast: 'ui message'}
            });
        }
    });

});
