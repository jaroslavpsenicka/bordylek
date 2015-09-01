app.registerCtrl('CommsCtrl', ['$scope', '$routeParams', '$http', function ($scope, $routeParams, $http) {

	$http.get('/rest/comm/' + $routeParams.commId, {
	}).then(function(response) {
		$scope.comm = response.data;
	}, function(error) {
		alert(error);
	});
}]);

app.registerCtrl('NewCommCtrl', ['$scope', '$q', 'userService', function ($scope, $q, userService) {

	userService.me(function(it) {
		$scope.newComm.name = it.user.name;
		$scope.newComm.location = it.user.location;
	});

    $scope.submit = function() {
		console.log($scope.newComm);
	};

}]);