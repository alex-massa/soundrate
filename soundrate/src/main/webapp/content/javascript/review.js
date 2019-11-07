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
        userReview.classList.remove('invisible');
        reviewForm.classList.add('invisible');
    } else {
        userReview.classList.add('invisible');
        reviewForm.classList.remove('invisible');
    }
}

function attachEventsToReviewForm() {
    let reviewRating = document.getElementById('review-rating');
    $(reviewRating).rating();

    let publishReviewButton = document.getElementById('publish-review-button');
    publishReviewButton.addEventListener('click', () => {
        let albumId = userReview.dataset.album;
        let rating = $(reviewRating).rating('get rating');
        let content = $(reviewForm).form('get value', 'content');
        $.ajax({
            method: 'POST',
            url: 'publish-review',
            data: {album: albumId, rating: rating, content: content},
            beforeSend: xhr => {
                $(reviewForm).form('validate form');
                if (!$(reviewForm).form('is valid'))
                    xhr.abort();
            }
        })
        .done(() => {
            userReview.dataset.published = JSON.stringify(true);
            userReview.querySelector('[data-user-review-rating]').textContent = $(reviewRating).rating('get rating');
            userReview.querySelector('[data-user-review-content]').textContent = $(reviewForm).form('get value', 'content');
            updateReviewAndFormVisibility();
            $('body').toast({
                message: 'Review successfully published',
                position: 'bottom right',
                class: 'success',
                className: {toast: 'ui message'}
            });
        })
        .fail(xhr => {
            if (xhr.statusText === 'canceled')
                return;
            let toastMessage = xhr.responseText || 'An unknown error occurred, please try again';
            $('body').toast({
                message: toastMessage,
                position: 'bottom right',
                class: 'error',
                className: {toast: 'ui message'}
            });
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
       let albumId = userReview.dataset.album;
       $.ajax({
          method: 'POST',
          url: 'delete-review',
          data: {album: albumId}
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
