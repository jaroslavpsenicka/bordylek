app.registerCtrl('CommsCtrl', ['$scope', '$routeParams', function ($scope, $routeParams) {
	$scope.comm = {
		name: $routeParams.commId
	}
}]);
