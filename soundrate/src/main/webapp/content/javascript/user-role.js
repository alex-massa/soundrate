window.addEventListener('load', () => {

    let roleSelect = document.getElementById('role-select');
    if (!roleSelect)
        return;
    $(roleSelect).dropdown({
        onChange: () => updateRoleButton.disabled = false
    });

    let updateRoleButton = document.getElementById('update-role-button');
    updateRoleButton.disabled = true;
    updateRoleButton.addEventListener('click', () => {
        let username = roleSelect.dataset.username;
        let role = $(roleSelect).dropdown('get value');
        $.ajax({
            url: 'update-user-role',
            method: 'post',
            data: {username: username, role: role},
            beforeSend: () => updateRoleButton.disabled = true
        })
        .done(() => {
            $('body').toast({
                message: 'The user role has been updated',
                position: 'bottom right',
                class: 'success',
                className: {toast: 'ui message'}
            });
        })
        .fail(xhr => {
            updateRoleButton.disabled = false;
            let toastMessage = xhr.responseText || 'An unknown error occurred, please try again';
            $('body').toast({
                message: toastMessage,
                position: 'bottom right',
                class: 'error',
                className: {toast: 'ui message'}
            });
        });
    });

});
