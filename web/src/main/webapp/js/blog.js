app.registerCtrl('BlogCtrl', function ($scope, $routeParams, blogService) {

	blogService.posts(function(response) {
		$scope.posts = response.items;
	});

});

