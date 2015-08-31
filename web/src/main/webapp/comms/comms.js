app.registerCtrl('CommsCtrl', ['$scope', '$routeParams', '$http', function ($scope, $routeParams, $http) {

	$http.get('/rest/comm/' + $routeParams.commId, {
	}).then(function(response) {
		$scope.comm = response.data;
	}, function(error) {
		alert(error);
	});
}]);

app.registerCtrl('NewCommCtrl', ['$scope', '$rootScope', '$http', function ($scope, $rootScope, $http) {

	$scope.newComm.name = $rootScope.user.location;
	$scope.newComm.location = $rootScope.user.location;

    $scope.submit = function() {
		console.log(newComm);
	};

}]);