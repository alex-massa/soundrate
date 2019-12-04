let reviewForm;
let userReview;
let deleteReviewModal;

window.addEventListener('load', () => {
    reviewForm = document.getElementById('review-form');
    if (!reviewForm)
        return;
    userReview = document.getElementById('user-review');
    deleteReviewModal = document.getElementById('delete-review-modal');
    attachEventsToReviewForm();
    attachClickEventsToUserReviewButtons();
    attachClickEventToConfirmReviewDeletionButton();
});

function updateReviewAndFormVisibility() {
    if (JSON.parse(userReview.dataset.published)) {
        userReview.classList.remove('hidden');
        reviewForm.classList.add('hidden');
    } else {
        userReview.classList.add('hidden');
        reviewForm.classList.remove('hidden');
    }
}

function attachEventsToReviewForm() {
    let reviewRating = document.getElementById('review-rating');
    $(reviewRating).rating();

    let publishReviewButton = document.getElementById('publish-review-button');
    publishReviewButton.addEventListener('click', () => {
        let reviewerUsername = userReview.dataset.reviewer;
        let reviewedAlbumId = userReview.dataset.album;
        let content = $(reviewForm).form('get value', 'content');
        let rating = $(reviewRating).rating('get rating');
        $.ajax({
            method: 'post',
            url: 'publish-review',
            data: {reviewer: reviewerUsername, album: reviewedAlbumId, content: content, rating: rating},
            beforeSend: xhr => {
                if (!$(reviewForm).form('is valid')) {
                    $(reviewForm).form('validate form');
                    xhr.abort();
                }
            }
        })
        .done(() => {
            $(reviewForm).form('validate form');
            userReview.dataset.published = JSON.stringify(true);
            userReview.querySelector('[data-user-review-content]').textContent = content;
            userReview.querySelector('[data-user-review-rating]').textContent = rating;
            updateReviewAndFormVisibility();
            showToast('Review successfully published', status.SUCCESS);
        })
        .fail(xhr => {
            if (xhr.statusText === 'canceled')
                return;
            let errorMessage = xhr.responseText || 'An unknown error occurred, please try again';
            showToast(errorMessage, status.ERROR);
        })
    });

    $(reviewForm).form({
        on: 'blur',
        fields: {
            content: {
                identifier: 'review-content',
                rules: [{
                    type: 'empty',
                    prompt: 'The review must not be empty!'
                }]
            }
        }
    });
}

function attachClickEventsToUserReviewButtons() {
    userReview.querySelector('[name="edit-review-button"]').addEventListener('click', () => {
        userReview.dataset.published = JSON.stringify(false);
        updateReviewAndFormVisibility();
    });
    userReview.querySelector('[name="delete-review-button"]').addEventListener('click', () => {
        $(deleteReviewModal).modal('show');
    });
}

function attachClickEventToConfirmReviewDeletionButton() {
    let deleteReviewConfirmButton = deleteReviewModal.querySelector('[data-delete-review]');
    deleteReviewConfirmButton.addEventListener('click', () => {
        let reviewerUsername = userReview.dataset.reviewer;
        let reviewedAlbumId = userReview.dataset.album;
        $.ajax({
            method: 'post',
            url: 'delete-review',
            data: {reviewer: reviewerUsername, album: reviewedAlbumId}
        })
        .done(() => {
            let reviewRating = document.getElementById('review-rating');
            applyReviewButtonsVisualChanges(userReview, null);
            userReview.querySelector('[data-text]').dataset.text = JSON.stringify(0);
            userReview.querySelector('[data-user-review-rating]').textContent = '';
            userReview.querySelector('[data-user-review-content]').textContent = '';
            reviewForm.querySelector('textarea[name="content"]').value = '';
            userReview.dataset.published = JSON.stringify(false);
            $(reviewRating).rating('set rating', 5);
            updateReviewAndFormVisibility();
            showToast('Review successfully deleted', status.SUCCESS);
        })
        .fail(xhr => {
            let errorMessage = xhr.responseText || 'An unknown error occurred, please try again';
            showToast(errorMessage, status.ERROR);
        });
    });
}
