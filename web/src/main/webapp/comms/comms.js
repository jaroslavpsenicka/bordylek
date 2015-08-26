app.registerCtrl('CommsCtrl', ['$scope', '$routeParams', '$http', function ($scope, $routeParams, $http) {
	$http.get('/rest/comm/' + $routeParams.commId, {
	}).then(function(response) {
		$scope.comm = response.data;
	}, function(error) {
		alert(error);
	});

}]);
