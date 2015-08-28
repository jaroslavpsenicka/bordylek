app.registerCtrl('ProfileCtrl', ['$scope', '$rootScope', '$http', function ($scope, $rootScope, $http) {
	$http.get('/rest/comm').then(function(response) {
		$rootScope.user.nearby = response.data;
	}, function(error) {
		console.log(error);
	});
}]);
