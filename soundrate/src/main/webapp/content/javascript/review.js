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
    attachEventsToUserReview();
    attachEventsToModal();
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

    $(reviewForm)
        .form({
            on: 'blur',
            fields: {
                content: {
                    identifier: 'review-content',
                    rules: [
                        {
                            type: 'empty',
                            prompt: 'The review must not be empty!'
                        }
                    ]
                }
            }
        })
        .api({
            action: 'publish review',
            method: 'POST',
            dataType: 'text',
            beforeSend: settings => {
                settings.urlData.album = userReview.dataset.album;
                settings.urlData.rating = $(reviewRating).rating('get rating');
                settings.urlData.content = $(reviewForm).form('get value', 'content');
                return settings;
            },
            onSuccess: () => {
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

function attachEventsToUserReview() {
    userReview.querySelector('[name="edit-review-button"]').addEventListener('click', () => {
        userReview.dataset.published = JSON.stringify(false);
        updateReviewAndFormVisibility();
    });
    userReview.querySelector('[name="delete-review-button"]').addEventListener('click', () => {
        $(deleteReviewModal).modal('show');
    });
}

function attachEventsToModal() {
    let deleteReviewConfirmButton = deleteReviewModal.querySelector('[data-delete-review]');
    $(deleteReviewConfirmButton).api({
        action: 'delete review',
        method: 'POST',
        dataType: 'text',
        beforeSend: settings => {
            settings.urlData.album = userReview.dataset.album;
            return settings;
        },
        onSuccess: () => {
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
