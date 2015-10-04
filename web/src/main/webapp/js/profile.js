app.registerCtrl('ProfileCtrl', ['$scope', 'userService', function ($scope, userService) {

	$scope.userData = userService.me();

}]);
