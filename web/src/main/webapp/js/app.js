
var app = angular.module('bordylekApp', [
  'services', 'filters', 'localize',
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
		templateUrl: "home.html",
		controller: "HomeCtrl"
	}).when("/profile", {
		templateUrl: "profile.html",
		controller: "ProfileCtrl",
		resolve: {
			deps: app.resolveDeps(['js/profile.js'])
		}
	}).when("/comms/create", {
		templateUrl: "comms-create.html",
		controller: "NewCommCtrl",
		resolve: {
			deps: app.resolveDeps(['js/comms.js'])
		}
	}).when("/comms/:commId", {
		templateUrl: "comms.html",
		controller: "CommsCtrl",
		resolve: {
			deps: app.resolveDeps(['js/comms.js'])
		}
	}).when("/blog", {
		templateUrl: "blog.html",
		controller: "BlogCtrl",
		resolve: {
			deps: app.resolveDeps(['js/blog.js'])
		}
	}).otherwise("/404", {
		templateUrl: "404.html",
		controller: "PageCtrl"
	});

}]);

app.run(["$http", "$rootScope", "$q", "$modal", "userService", function($http, $rootScope, $q, $modal, userService) {
	var language = window.navigator.userLanguage || window.navigator.language;
	if (language) {
		$http({url: '/messages-' + language + '.json'}).success(function(messages) {
			window.i18n = messages;
		});
	}

	userService.me(function(response) {
		var user = response.user;
		if (user.status == 'NEW') {
			$modal.open({
				templateUrl: 'template/welcome.tpl.html',
				backdrop : 'static',
				keyboard: false,
				controller: function ($scope, $modalInstance, $http) {
					$scope.getLocation = function(val) {
						return $http.get('//maps.googleapis.com/maps/api/geocode/json', {
							params: {
								address: val,
								sensor: false
							}
						}).then(function(response) {
							return response.data.results;
						});
					};

					$scope.newUser = {
						id: user.id,
						name: user.name
					};

					$scope.submit = function() {
						var newUser = {
							name: $scope.newUser.name,
							location: {
								id: $scope.newUser.location.place_id,
								name: $scope.newUser.location.formatted_address,
								lat: $scope.newUser.location.geometry.location.lat,
								lng: $scope.newUser.location.geometry.location.lng
							}
						};
						userService.update({id: $scope.newUser.id}, newUser, function() {
							window.location.reload();
						});
					};
				}
			}).result.then(function() {
				// $window.location.reload();
		   	});
		}
	});

}]);

app.controller('HeaderCtrl', function ($scope, userService) {
	userService.me(function(response) {
		$scope.userData = response;
	});
});

app.controller('HomeCtrl', function ($scope, userService, $modal) {
	userService.me(function(response) {
		$scope.userData = response;
	}, function(error) {
		$scope.userData = {};
	});

});
