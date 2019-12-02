window.addEventListener('load', () => {

    let review = document.querySelector('[data-type="review"]');
    if (!review)
        return;
    let deleteReviewButton = review.querySelector('[name="delete-review-button"]');
    if (deleteReviewButton) {
        let deleteReviewModal = document.getElementById('delete-review-modal');
        deleteReviewButton.addEventListener('click', () => $(deleteReviewModal).modal('show'));
        let deleteReviewConfirmButton = deleteReviewModal.querySelector('[data-delete-review]');
        deleteReviewConfirmButton.addEventListener('click', () => {
            let reviewerUsername = review.dataset.reviewer;
            let reviewedAlbumId = review.dataset.album;
            $.ajax({
                method: 'post',
                url: 'delete-review',
                data: {reviewer: reviewerUsername, album: reviewedAlbumId}
            })
            .done(() => {
                $('body').toast({
                    message: 'Review successfully deleted',
                    position: 'bottom right',
                    class: 'success',
                    className: {toast: 'ui message'}
                });
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

});