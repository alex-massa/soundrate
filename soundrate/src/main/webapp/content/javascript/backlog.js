const toggleInBacklogButtonStates = {
    POS: '<i class="green add icon"></i> Insert in listening backlog',
    NEG: '<i class="red minus icon"></i> Remove from listening backlog'
};

window.addEventListener('load', () => {
    let user = document.querySelector('[data-user]');
    if (!user)
        return;
    let albums = document.querySelectorAll('[data-type="album"]');
    albums.forEach(album => {
        isAlbumInBacklog(user, album);
        attachClickEventToToggleInBacklogButton(user, album);
    });
});

function isAlbumInBacklog(user, album) {
    let username = user.dataset.user;
    let albumId = album.dataset.album;
    $.ajax({
        url: 'is-album-in-user-backlog',
        method: 'get',
        data: {user: username, album: albumId}
    })
    .done(data => {
        let button = album.querySelector('[data-backlog]');
        button.dataset.backlog = data;
        applyVisualChangesToToggleInBacklogButton(album);
    })
    .fail(xhr => {
        let toastMessage = xhr.responseText || 'An unknown error occurred, please try again';
        $('body').toast({
            message: toastMessage,
            position: 'bottom right',
            class: 'error',
            className: {toast: 'ui message'}
        });
    });
}

function attachClickEventToToggleInBacklogButton(user, album) {
    let button = album.querySelector('[data-backlog]');
    button.addEventListener('click', () => {
        let username = user.dataset.user;
        let albumId = album.dataset.album;
        $.ajax({
            method: 'post',
            url: 'update-user-backlog',
            data: {user: username, album: albumId}
        })
        .done(() => {
            button.dataset.backlog = JSON.stringify(!JSON.parse(button.dataset.backlog));
            applyVisualChangesToToggleInBacklogButton(album);
        })
        .fail(xhr => {
            let toastMessage = xhr.responseText || 'An unknown error occurred, please try again';
            $('body').toast({
                message: toastMessage,
                position: 'bottom right',
                class: 'error',
                className: {toast: 'ui message'}
            });
        });
    });
}

function applyVisualChangesToToggleInBacklogButton(album) {
    let toggleButton = album.querySelector('[data-backlog]');
    toggleButton.innerHTML = JSON.parse(toggleButton.dataset.backlog)
        ? toggleInBacklogButtonStates.NEG
        : toggleInBacklogButtonStates.POS;
}
