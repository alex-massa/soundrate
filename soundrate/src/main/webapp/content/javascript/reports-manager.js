let reportedReviewsTable;
let selectedReportedReview;
let deleteReviewButton;
let deleteReviewReportsButton;

window.addEventListener('load', () => {
    let table = document.getElementById('reports-table');
    if (!table)
        return;

    // @todo add a column that displays the number of reports per review
    reportedReviewsTable = $(table).DataTable({
        paging: false,
        scrollY: 420,
        order: [[ 1, 'asc' ]],
        select: {
            style: 'single',
            info: false
        },
        columns: [
            {render: (data, type, row) =>
                    $('<a/>', {href: `review?reviewer=${row["reviewerUsername"]}&album=${row["reviewedAlbumId"]}`})
                        .append($('<i/>', {class: 'ui external icon'})).prop('outerHTML')
            },
            {data: 'reviewerUsername'},
            {data: 'reviewedAlbumId'},
            {data: 'rating'},
            {data: 'publicationDate'}
        ]
    });

    reportedReviewsTable.on('select', (e, dt, type, index) =>
        selectedReportedReview = reportedReviewsTable.row(index).data());

    deleteReviewButton = document.querySelector('[name="delete-review-button"]');
    deleteReviewReportsButton = document.querySelector('[name="delete-reports-button"]');

    deleteReviewButton.addEventListener('click', () => {
        if (!selectedReportedReview) {
            showToast('A review must be selected in order to proceed', status.ERROR);
            return;
        }
        disableControlButtons();
        try {
            deleteReview(selectedReportedReview);
            showToast('The selected review was deleted', status.SUCCESS);
        } catch (e) {
            showToast(e, status.ERROR);
        }
        enableControlButtons();

        updateReportedReviewsTable();
    });

    deleteReviewReportsButton.addEventListener('click', () => {
        if (!selectedReportedReview) {
            showToast('A review must be selected in order to proceed', status.ERROR);
            return;
        }
        disableControlButtons();
        try {
            deleteReviewReports(selectedReportedReview);
            showToast('The reports for the selected review were deleted', status.SUCCESS);
        } catch (e) {
            showToast(e, status.ERROR);
        }
        enableControlButtons();

        updateReportedReviewsTable();
    });

    updateReportedReviewsTable();
});

function disableControlButtons() {
    deleteReviewButton.disabled = true;
    deleteReviewReportsButton.disabled = true;
}

function enableControlButtons() {
    deleteReviewButton.disabled = false;
    deleteReviewReportsButton.disabled = false;
}

function updateReportedReviewsTable() {
    try {
        setReportedReviewsTableContent(getReportedReviews());
        selectedReportedReview = undefined;
    } catch (e) {
        showToast(e, status.ERROR);
    }
}

function setReportedReviewsTableContent(reportedReviews) {
    if (!reportedReviews)
        reportedReviewsTable.clear().draw();
    else
        reportedReviewsTable.clear().rows.add(reportedReviews).draw();
}

function getReportedReviews() {
    let reviews;
    $.ajax({
        cache: false,
        method: 'get',
        url: 'get-reported-reviews',
        async: false,
        dataType: 'json'
    })
    .done(reportedReviews => reviews = reportedReviews)
    .fail(xhr => {
        throw xhr.responseText || 'An unknown error occurred, please try again'
    });
    return reviews;
}

function deleteReview(review) {
    let reviewerUsername = review.reviewerUsername;
    let reviewedAlbumId = review.reviewedAlbumId;
    $.ajax({
        method: 'post',
        url: 'delete-review',
        data: {reviewer: reviewerUsername, album: reviewedAlbumId},
        async: false
    })
    .fail(xhr => {
        throw xhr.responseText || 'An unknown error occurred, please try again'
    });
}

function deleteReviewReports(review) {
    let reviewerUsername = review.reviewerUsername;
    let reviewedAlbumId = review.reviewedAlbumId;
    $.ajax({
        method: 'post',
        url: 'delete-review-reports',
        data: {reviewer: reviewerUsername, album: reviewedAlbumId},
        async: false
    })
    .fail(xhr => {
        throw xhr.responseText || 'An unknown error occurred, please try again'
    });
}
