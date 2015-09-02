app.registerCtrl('CommsCtrl', ['$scope', '$routeParams', '$http', function ($scope, $routeParams, $http) {

	$http.get('/rest/comm/' + $routeParams.commId, {
	}).then(function(response) {
		$scope.comm = response.data;
	}, function(error) {
		alert(error);
	});
}]);

app.registerCtrl('NewCommCtrl', ['$scope', '$q', 'commService', function ($scope, $q, commService) {

	$scope.newComm = {};

	userService.me(function(it) {
    	var commaIdx = it.user.location.indexOf(',');
		$scope.newComm.title = (commaIdx > -1) ?
			it.user.location.substring(0, commaIdx - 1) : it.user.location;
		$scope.newComm.location = it.user.location;
	});

    $scope.submit = function() {
		commService.create({}, $scope.newComm, function() {
			window.location.href = '/#/profile';
		});
	};

}]);