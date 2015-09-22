
var app = angular.module('monitorApp', [
  'services', 'filters',
  'ui.bootstrap', 'ngRoute', 'ngAnimate', 'angular.filter',
  'highcharts-ng'
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

app.filter('', function() {
  return function(input) {
    return input ? '\u2713' : '\u2718';
  };
});


app.controller('PageCtrl', function (/* $scope, $location, $http */) {
});

app.controller('HeaderCtrl', function ($scope, $routeParams, $modal, chartsService) {

    $scope.createChart = function() {
        $modal.open({
            templateUrl: 'template/create-chart.tpl.html',
            controller: function ($scope, $modalInstance) {
                $scope.chart = { "size": "col-sm-3" };
                $scope.submit = function () {
                    $modalInstance.close($scope.chart);
                }
                $scope.cancel = function () {
                    $modalInstance.dismiss('cancel');
                };
            }
        }).result.then(function(value) {
            chartsService.save(value);
        });
    };

});
