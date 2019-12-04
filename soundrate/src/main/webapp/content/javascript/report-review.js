window.addEventListener('load', () => {
    let user = document.querySelector('[data-user]');
    if (!user)
        return;
    let reviews = document.querySelectorAll('[data-type="review"]');
    reviews.forEach(review => {
        let button = review.querySelector('[data-report]');
        if (!button)
            return;
        isReviewReportedByUser(user, review);
        attachClickEventToReportReviewButton(user, review);
    });
});

function isReviewReportedByUser(reporter, review) {
    let reporterUsername = reporter.dataset.user;
    let reviewerUsername = review.dataset.reviewer;
    let reviewedAlbumId = review.dataset.album;
    $.ajax({
        method: 'get',
        url: 'is-review-reported-by-user',
        data: {reporter: reporterUsername, reviewer: reviewerUsername, album: reviewedAlbumId}
    })
    .done(data => {
        let reported = JSON.parse(data);
        let button = review.querySelector('[data-report]');
        button.disabled = reported;
    })
    .fail(xhr => {
        let errorMessage = xhr.responseText || 'An unknown error occurred, please try again';
        showToast(errorMessage, status.ERROR);
    });
}

function attachClickEventToReportReviewButton(reporter, review) {
    let button = review.querySelector('[data-report]');
    button.addEventListener('click', () => {
        let reporterUsername = reporter.dataset.user;
        let reviewerUsername = review.dataset.reviewer;
        let reviewedAlbumId = review.dataset.album;
        $.ajax({
           method: 'post',
           url: 'report-review',
           data: {reporter: reporterUsername, reviewer: reviewerUsername, album: reviewedAlbumId}
        })
        .done(() => {
            button.disabled = true;
            showToast('The review has been reported', status.SUCCESS);
        })
        .fail(xhr => {
            let errorMessage = xhr.responseText || 'An unknown error occurred, please try again';
            showToast(errorMessage, status.ERROR);
        });
    });
}
