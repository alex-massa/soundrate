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
        .done(() => showToast('The user role has been updated', status.SUCCESS))
        .fail(xhr => {
            updateRoleButton.disabled = false;
            let errorMessage = xhr.responseText || 'An unknown error occurred, please try again';
            showToast(errorMessage, status.ERROR);
        });
    });
});
