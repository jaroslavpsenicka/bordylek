angular.module('blog', [
  'services', 'localize',
  'ui.bootstrap', 'ngRoute', 'ngAnimate'
])

.controller('BlogCtrl', function ($scope, $routeParams, blogService) {

	blogService.posts(function(response) {
		$scope.posts = response.items;
	});

});

