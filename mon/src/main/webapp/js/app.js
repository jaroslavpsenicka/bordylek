
var app = angular.module('monitorApp', [
  'services',
  'ui.bootstrap', 'ngRoute', 'ngAnimate'
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

app.controller('PageCtrl', function (/* $scope, $location, $http */) {
});