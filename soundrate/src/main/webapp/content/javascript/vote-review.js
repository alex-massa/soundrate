const voteButtonsStates = {
    POS: 'positive',
    NEG: 'negative'
};

window.addEventListener('load', () => {
    let reviews = document.querySelectorAll('[data-type="review"]');
    reviews.forEach(review => {
        if (JSON.parse(review.dataset.voteEnabled)) {
            attachButtonsCalls(review);
            if (JSON.parse(review.dataset.published))
                getReviewVote(review);
        }
    });
});

function getReviewVote(review) {
    let reviewerUsername = review.dataset.reviewer;
    let albumId = review.dataset.album;
    $.ajax({
        method: 'get',
        dataType: 'json',
        url: 'get-review-vote',
        data: {'reviewer': reviewerUsername, 'album': albumId}
    })
        .done(data => {
            let vote = data == null ? null : data.vote;
            applyReviewButtonsVisualChanges(review, vote);
            setReviewVoteValue(review, vote);
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

function attachButtonsCalls(review) {
    review.querySelectorAll('button[data-value]').forEach(button => {
        $(button).api({
            action: 'vote review',
            method: 'POST',
            dataType: 'text',
            beforeSend: settings => {
                let vote = getVoteValueToSet(review.dataset.vote, button.dataset.value);
                if (vote == null)
                    delete settings.urlData.vote;
                else
                    settings.urlData.vote = vote;
                settings.urlData.album = review.dataset.album;
                settings.urlData.reviewer = review.dataset.reviewer;
                return settings;
            },
            onSuccess: () => {
                let vote = getVoteValueToSet(review.dataset.vote, button.dataset.value);
                applyReviewButtonsVisualChanges(review, vote);
                applyReviewUpvotesVisualChanges(review, vote);
                setReviewVoteValue(review, vote);
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
    });
}

function getVoteValueToSet(reviewVote, buttonValue) {
    return reviewVote === buttonValue ? null : buttonValue;
}

function applyReviewButtonsVisualChanges(review, newVote) {
    newVote = JSON.parse(newVote);
    let posButton = review.querySelector('button[data-value="true"]');
    let negButton = review.querySelector('button[data-value="false"]');
    switch (newVote) {
        case null:
            posButton.classList.remove(voteButtonsStates.POS);
            negButton.classList.remove(voteButtonsStates.NEG);
            break;
        case true:
            posButton.classList.add(voteButtonsStates.POS);
            negButton.classList.remove(voteButtonsStates.NEG);
            break;
        case false:
            posButton.classList.remove(voteButtonsStates.POS);
            negButton.classList.add(voteButtonsStates.NEG);
            break;
    }
}

function applyReviewUpvotesVisualChanges(review, newVote) {
    newVote = JSON.parse(newVote);
    let currentVote = JSON.parse(review.dataset.vote || null);
    let reviewScore = JSON.parse(review.querySelector('[data-text]').dataset.text);
    if (currentVote && newVote == null)
        reviewScore -= 1;
    else if (currentVote === true && newVote === false)
        reviewScore -= 2;
    else if (currentVote === false && newVote == null)
        reviewScore += 1;
    else if (currentVote === false && newVote === true)
        reviewScore += 2;
    else if (currentVote == null && newVote === true)
        reviewScore += 1;
    else if (currentVote == null && newVote === false)
        reviewScore -= 1;
    review.querySelector('[data-text]').dataset.text = reviewScore;
}

function setReviewVoteValue(review, newVote) {
    review.dataset.vote = newVote == null ? null : newVote;
}
