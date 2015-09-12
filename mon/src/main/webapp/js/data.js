app.registerCtrl('DataCtrl', ['$scope', 'metricsService', function ($scope, metricsService) {

	metricsService.meters(function(response) {
		$scope.meters = stripPackage(response.data);
	});

	metricsService.counters(function(response) {
		$scope.counters = stripPackage(response.data);
	});

	metricsService.timers(function(response) {
		$scope.timers = stripPackage(response.data);
	});

	metricsService.gauges(function(response) {
		$scope.gauges = stripPackage(response.data);
	});

	metricsService.histograms(function(response) {
		$scope.histograms = stripPackage(response.data);
	});

	stripPackage = function(objects) {
		for (var i = 0; i < objects.length; i++) {
			var name = objects[i].name;
			if (name.indexOf('org.bordylek.web.') == 0) {
				objects[i].name = name.substring('org.bordylek.web.'.length);
			}
		}

		return objects;
	}

}]);
