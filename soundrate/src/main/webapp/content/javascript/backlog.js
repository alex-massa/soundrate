const toggleInBacklogButtonStates = {
    POS: '<i class="green add icon"></i> Inserisci in coda d\'ascolto',
    NEG: '<i class="red minus icon"></i> Rimuovi da coda d\'ascolto'
};

window.addEventListener('load', () => {
    let albums = document.querySelectorAll('[data-type="album"][data-enabled="true"]');
    albums.forEach(album => {
        isalbumInBacklog(album);
        attachToggleInBacklogButtonCall(album);
    });
});

function isalbumInBacklog(album) {
    let albumId = album.dataset.album;
    $.ajax({
        dataType: 'json',
        url: 'is-album-in-user-backlog',
        data: {'album': albumId}
    })
        .done(data => {
            let button = album.querySelector('[data-backlog]');
            button.dataset.backlog = data;
            applyToggleInBacklogButtonVisualChanges(album);
        })
        .fail(xhr => {
            let toastMessage = xhr.responseText ? xhr.responseText : 'An unknown error occurred, please try again';
            $('body').toast({
                message: toastMessage,
                position: 'bottom right',
                class: 'error',
                className: {toast: 'ui message'}
            });
        });
}

function attachToggleInBacklogButtonCall(album) {
    let button = album.querySelector('[data-backlog]');
    $(button).api({
        action: 'toggle in backlog',
        method: 'POST',
        dataType: 'text',
        beforeSend: settings => {
            settings.urlData.album = album.dataset.album;
            return settings;
        },
        onSuccess: () => {
            button.dataset.backlog = JSON.stringify(!JSON.parse(button.dataset.backlog));
            applyToggleInBacklogButtonVisualChanges(album);
        },
        onError: (errorMessage, element, xhr) => {
            let toastMessage = xhr.responseText ? xhr.responseText : 'An unknown error occurred, please try again';
            $('body').toast({
                message: toastMessage,
                position: 'bottom right',
                class: 'error',
                className: {toast: 'ui message'}
            });
        }
    });
}

function applyToggleInBacklogButtonVisualChanges(album) {
    let toggleButton = album.querySelector('[data-backlog]');
    toggleButton.innerHTML = JSON.parse(toggleButton.dataset.backlog) ?
        toggleInBacklogButtonStates.NEG :
        toggleInBacklogButtonStates.POS;
}
