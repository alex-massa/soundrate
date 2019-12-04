const status = {
    SUCCESS: 'success',
    ERROR: 'error'
};

function showToast(message, status) {
    $('body').toast({
        message: message,
        position: 'bottom right',
        class: status,
        className: {toast: 'ui message'}
    });
}
