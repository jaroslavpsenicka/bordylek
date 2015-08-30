
var app = angular.module('tutorialWebApp', [
  'ngRoute', 'ngAnimate',
  'localize',
  'ui.bootstrap'
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
			templateUrl: "partials/home.html",
			controller: "PageCtrl"
		}).when("/login", {
			templateUrl: "login/login.html",
			controller: "PageCtrl"
		}).when("/profile", {
			templateUrl: "profile/profile.html",
			controller: "ProfileCtrl",
		  	resolve: {
		  		deps: app.resolveDeps(['profile/profile.js'])
			}
		}).when("/comms/:commId", {
			templateUrl: "comms/comm.html",
			controller: "CommsCtrl",
		  	resolve: {
		  		deps: app.resolveDeps(['comms/comms.js'])
			}
		})

		.when("/about", {templateUrl: "partials/about.html", controller: "PageCtrl"})
		.when("/faq", {templateUrl: "partials/faq.html", controller: "PageCtrl"})
		.when("/pricing", {templateUrl: "partials/pricing.html", controller: "PageCtrl"})
		.when("/contact", {templateUrl: "partials/contact.html", controller: "PageCtrl"})
		.when("/blog", {templateUrl: "partials/blog.html", controller: "BlogCtrl"})
		.when("/blog/post", {templateUrl: "partials/blog_item.html", controller: "BlogCtrl"})
		.otherwise("/404", {templateUrl: "partials/404.html", controller: "PageCtrl"});
}]);

app.run(["$http", "$rootScope", function($http, $rootScope) {
	var language = window.navigator.userLanguage || window.navigator.language;
	if (language) {
		$http({url: '/messages-' + language + '.json'}).success(function(messages) {
			window.i18n = messages;
		});
	}
}]);

app.controller('HeaderCtrl', function ($rootScope, $http) {
  	$http({url: '/rest/user/me'}).success(function(user) {
  		$rootScope.user = user;
		console.log($rootScope.user);
		$http.get('/rest/comm').then(function(response) {
			$rootScope.user.nearby = response.data;
			console.log($rootScope.user);
		});
  	});
});

app.controller('WelcomeCtrl', function ($rootScope, $http, $scope) {
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

    $scope.submit = function() {
    	var user = $rootScope.user;
		$http.post('/rest/user/' + user.id, {
			name: user.name,
			location: {
				id: user.location.place_id,
				name: user.location.formatted_address,
				lat: user.location.geometry.location.lat,
				lng: user.location.geometry.location.lng
			}
		}).then(function(response) {
			$rootScope.user = response.data;
		}, function(error) {
			alert(error);
		});
	};
});

/**
 * Controls the Blog
 */
app.controller('BlogCtrl', function (/* $scope, $location, $http */) {
  console.log("Blog Controller reporting for duty.");
});

/**
 * Controls all other Pages
 */
app.controller('PageCtrl', function (/* $scope, $location, $http */) {
  console.log("Page Controller reporting for duty.");

  // Activates the Carousel
  $('.carousel').carousel({
    interval: 5000
  });

  // Activates Tooltips for Social Links
  $('.tooltip-social').tooltip({
    selector: "a[data-toggle=tooltip]"
  })
});