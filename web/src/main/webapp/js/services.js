var services = angular.module('services', ['ngResource']);

services.factory('userService', function($resource) {
    return $resource('', {id: '@id'}, {
        me: {
            url: 'rest/user/me',
            method: 'GET',
            cache: true
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

services.factory('blogService', function($resource) {
    return $resource('', {}, {
        blog: {
            url: 'rest/blog',
            method: 'GET',
            cache: true
        },
        posts: {
            url: 'rest/blog/posts',
            method: 'GET',
            cache: true
        }
    });
});