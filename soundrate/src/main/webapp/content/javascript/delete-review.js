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
            .done(() => showToast('Review successfully deleted', status.SUCCESS))
            .fail(xhr => {
                let errorMessage = xhr.responseText || 'An unknown error occurred, please try again';
                showToast(errorMessage, status.ERROR);
            });
        });
    }
});