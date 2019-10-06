const searchPageEndpoint = 'search';

window.addEventListener('load', () => {
    let searchBar = document.getElementById('search-bar');
    $(searchBar).form({fields: {query: 'empty'}});
    searchBar.addEventListener('submit', event => {
        event.preventDefault();
        if (!$(searchBar).form('is valid', 'query'))
            return;
        let query = encodeURIComponent($(searchBar).form('get value', 'query'));
        window.location.href = `${searchPageEndpoint}?q=${query}`;
    });
});
