app.registerCtrl('MetricsCtrl', function ($scope, metricsService) {

	metricsService.meters(function(response) {
		$scope.meters = response.data;
	});

	metricsService.counters(function(response) {
		$scope.counters = response.data;
	});

	metricsService.timers(function(response) {
		$scope.timers = response.data;
	});

	metricsService.gauges(function(response) {
		$scope.gauges = response.data;
	});

	metricsService.histograms(function(response) {
		$scope.histograms = response.data;
	});


});
