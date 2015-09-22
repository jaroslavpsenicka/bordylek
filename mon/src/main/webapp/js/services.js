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

services.factory('alertsService', function($resource) {
    return $resource('', {id: '@id'}, {
        get: {
            url: 'rest/alerts',
            method: 'GET',
            isArray: true
        },
        resolveAlert: {
            url: 'rest/alerts/:id/resolve',
            method: 'POST'
        }
    });
});

services.factory('metricsService', function($resource) {
    return $resource('', {type: '@type', name: '@name'}, {
        data: {
            url: 'rest/metrics/:type/:name',
            method: 'GET'
        },
        meters: {
            url: 'rest/metrics/meter',
            method: 'GET'
        },
        counters: {
            url: 'rest/metrics/counter',
            method: 'GET'
        },
        timers: {
            url: 'rest/metrics/timer',
            method: 'GET'
        },
        gauges: {
            url: 'rest/metrics/gauge',
            method: 'GET'
        },
        histograms: {
            url: 'rest/metrics/histogram',
            method: 'GET'
        }
    });
});

services.factory('chartsService', function($resource) {
    return $resource('', {id: '@id'}, {
        get: {
            url: 'rest/charts',
            method: 'GET',
            isArray: true
        },
        save: {
            url: 'rest/charts',
            method: 'POST'
        },
        delete: {
            url: 'rest/charts/:id',
            method: 'DELETE'
        }
    });
});
