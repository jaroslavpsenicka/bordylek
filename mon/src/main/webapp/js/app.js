
var app = angular.module('monitorApp', [
  'services', 'filters', 'directives',
  'ui.bootstrap', 'ngRoute', 'ngAnimate', 'angular.filter',
  'highcharts-ng', 'ngFileUpload'
]);

app.config(['$routeProvider', '$controllerProvider', function ($routeProvider, $controllerProvider) {
	app.registerCtrl = $controllerProvider.register;
	app.resolveDeps = function(dependencies){
	  	return function($q, $rootScope) {
			var deferred = $q.defer();
			$script(dependencies, function() {
		  		$rootScope.$apply(function() {
					deferred.resolve();
		  		});
			});
			return deferred.promise;
	  	}
	};

	$routeProvider.when("/", {
		templateUrl: "/home.html",
		controller: "HomeCtrl",
		resolve: {
			deps: app.resolveDeps(['js/home.js'])
		}
	}).when("/metrics", {
		templateUrl: "/metrics.html",
		controller: "MetricsCtrl",
		resolve: {
			deps: app.resolveDeps(['js/metrics.js'])
		}
	}).when("/data", {
		templateUrl: "/data.html",
		controller: "DataCtrl",
		resolve: {
			deps: app.resolveDeps(['js/data.js'])
		}
	}).when("/rules", {
		templateUrl: "/rules.html",
		controller: "RulesCtrl",
		resolve: {
			deps: app.resolveDeps(['js/rules.js'])
		}
	}).otherwise("/404", {
		templateUrl: "/404.html",
		controller: "PageCtrl"
	});
}]);

app.controller('PageCtrl', function (/* $scope, $location, $http */) {
});

app.controller('HeaderCtrl', function ($scope, $route, $routeParams, $window, $modal, chartsService, userService, Upload) {

	userService.me(function(response) {
		$scope.user = response;
	});

    $scope.createChart = function() {
        $modal.open({
            templateUrl: 'template/create-chart.tpl.html',
            controller: function ($scope, $modalInstance) {
                $scope.chart = { "size": "col-md-3" };
                $scope.submit = function () {
                    $modalInstance.close($scope.chart);
                }
                $scope.cancel = function () {
                    $modalInstance.dismiss('cancel');
                };
            }
        }).result.then(function(value) {
            chartsService.create(value);
            $route.reload();
        });
    };

    $scope.saveCharts = function() {
		$window.open('rest/charts/save', '_blank');
	};

    $scope.$watch('files', function () {
        if ($scope.files) {
            $scope.loadCharts($scope.files);
        }
    });

    $scope.loadCharts = function(files) {
        angular.forEach(files, function(file) {
            Upload.upload({
                url: 'rest/charts/load',
                file: file
            }).success(function (data, status, headers, config) {
            	$route.reload();
            }).error(function (data, status) {
                $scope.handleError(status, data, "Error reading " + file.name, "reading the file " + file.name,
                    "Please make sure the file is a JSON file and is in proper format expected by the application.");
            });
        });
    }

});

