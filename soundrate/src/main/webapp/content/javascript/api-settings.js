// Responses without this status will trigger error conditions
$.fn.api.successTest = response => response.statusText.toString().startsWith('2');

// Define API endpoints once globally
$.fn.api.settings.api = {
    'sign up': 'sign-user?action=signup&username={username}&email={email}&password={password}',
    'sign in': 'sign-user?action=signin&username={username}&password={password}',
    'sign out': 'sign-user?action=signout',
    'vote review': 'vote-review?vote={/vote}&album={album}&reviewer={reviewer}',
    'toggle in backlog': 'update-user-backlog?album={album}',
    'publish review': 'update-review?action=publish&album={album}&rating={rating}&content={content}',
    'delete review': 'update-review?action=delete&album={album}'
};
