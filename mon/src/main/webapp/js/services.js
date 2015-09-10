var services = angular.module('services', ['ngResource']);

services.factory('rulesService', function($resource) {
    return $resource('', {}, {
        readRules: {
            url: 'rest/rules',
            method: 'GET'
        },
        toggleRule: {
            url: 'rest/rules/toggle',
            method: 'POST'
        }
    });
});

services.factory('alertService', function($resource) {
    return $resource('', {id: '@id'}, {
        get: {
            url: 'rest/alerts',
            method: 'GET'
        },
        resolveAlert: {
            url: 'rest/alerts/:id/resolve',
            method: 'POST'
        }
    });
});
