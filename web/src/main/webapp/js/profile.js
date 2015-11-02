angular.module('profile', [
  	'services'
])

.controller('ProfileCtrl', ['$scope', 'userService', function ($scope, userService) {

	$scope.userData = userService.me();

}]);
