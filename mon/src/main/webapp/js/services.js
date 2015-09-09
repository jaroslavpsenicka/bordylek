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
