app.registerCtrl('DataCtrl', ['$scope', 'metricsService', function ($scope, metricsService) {

	metricsService.meters(function(response) {
		$scope.meters = response.data;
	});

	metricsService.counters(function(response) {
		$scope.counters = response.data;
	});

	metricsService.timers(function(response) {
		$scope.timers = response.data;
	});

}]);
