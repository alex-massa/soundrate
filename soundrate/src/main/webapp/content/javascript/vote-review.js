const voteButtonsState = {
    POS: 'positive',
    NEG: 'negative'
};

window.addEventListener('load', () => {
    let user = document.querySelector('[data-user]');
    let reviews = document.querySelectorAll('[data-type="review"]');
    reviews.forEach(review => {
        if (user) {
            attachVoteButtonsClickEvent(user, review);
            if (JSON.parse(review.dataset.published))
                getReviewVoteValue(user, review);
        }
    });
});

function getReviewVoteValue(voter, review) {
    let voterUsername = voter.dataset.user;
    let reviewerUsername = review.dataset.reviewer;
    let reviewedAlbumId = review.dataset.album;
    $.ajax({
        method: 'get',
        url: 'get-review-vote-value',
        data: {voter: voterUsername, reviewer: reviewerUsername, reviewedAlbum: reviewedAlbumId}
    })
        .done(data => {
            let voteValue = !data ? null : JSON.parse(data);
            applyReviewButtonsVisualChanges(review, voteValue);
            setReviewVoteValue(review, voteValue);
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

function attachVoteButtonsClickEvent(voter, review) {
    review.querySelectorAll('button[data-value]').forEach(button => {
        button.addEventListener('click', () => {
            let voterUsername = voter.dataset.user;
            let reviewerUsername = review.dataset.reviewer;
            let reviewedAlbumId = review.dataset.album;
            let voteValue = getVoteValueToSet(review.dataset.vote, button.dataset.value);
            $.ajax({
                method: 'post',
                url: 'vote-review',
                data: {
                    voter: voterUsername,
                    reviewer: reviewerUsername,
                    reviewedAlbum: reviewedAlbumId,
                    vote: voteValue
                }
            })
                .done(() => {
                    let voteValue = getVoteValueToSet(review.dataset.vote, button.dataset.value);
                    applyReviewButtonsVisualChanges(review, voteValue);
                    applyReviewUpvotesVisualChanges(review, voteValue);
                    setReviewVoteValue(review, voteValue);
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
    });
}

function getVoteValueToSet(reviewVoteValue, buttonValue) {
    return reviewVoteValue === buttonValue ? null : buttonValue;
}

function applyReviewButtonsVisualChanges(review, newVoteValue) {
    newVoteValue = JSON.parse(newVoteValue);
    let posButton = review.querySelector('button[data-value="true"]');
    let negButton = review.querySelector('button[data-value="false"]');
    switch (newVoteValue) {
        case null:
            posButton.classList.remove(voteButtonsState.POS);
            negButton.classList.remove(voteButtonsState.NEG);
            break;
        case true:
            posButton.classList.add(voteButtonsState.POS);
            negButton.classList.remove(voteButtonsState.NEG);
            break;
        case false:
            posButton.classList.remove(voteButtonsState.POS);
            negButton.classList.add(voteButtonsState.NEG);
            break;
    }
}

function applyReviewUpvotesVisualChanges(review, newVoteValue) {
    newVoteValue = JSON.parse(newVoteValue);
    let currentVoteValue = JSON.parse(review.dataset.vote || null);
    let reviewScore = JSON.parse(review.querySelector('[data-text]').dataset.text);
    if (currentVoteValue && newVoteValue == null)
        reviewScore -= 1;
    else if (currentVoteValue === true && newVoteValue === false)
        reviewScore -= 2;
    else if (currentVoteValue === false && newVoteValue == null)
        reviewScore += 1;
    else if (currentVoteValue === false && newVoteValue === true)
        reviewScore += 2;
    else if (currentVoteValue == null && newVoteValue === true)
        reviewScore += 1;
    else if (currentVoteValue == null && newVoteValue === false)
        reviewScore -= 1;
    review.querySelector('[data-text]').dataset.text = reviewScore;
}

function setReviewVoteValue(review, newVoteValue) {
    review.dataset.vote = newVoteValue == null ? null : newVoteValue;
}
