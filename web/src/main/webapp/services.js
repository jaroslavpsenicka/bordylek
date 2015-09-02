var services = angular.module('services', ['ngResource']);

services.factory('userService', function($resource) {
    return $resource('', {id: '@id'}, {
        me: {
            url: 'rest/user/me',
            method: 'GET'
        },
        update: {
            url: 'rest/user/:id',
            method: 'POST'
        }
    });
});

services.factory('commService', function($resource) {
    return $resource('', {}, {
        create: {
            url: 'rest/comm',
            method: 'POST'
        }
    });
});

