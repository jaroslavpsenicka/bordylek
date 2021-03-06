angular.module('comms', [
  'services', 'localize',
  'ui.bootstrap', 'ngRoute', 'ngAnimate'
])

.controller('CommsCtrl', ['$scope', '$routeParams', '$http', function ($scope, $routeParams, $http) {

	$http.get('/rest/comm/' + $routeParams.commId, {
	}).then(function(response) {
		$scope.comm = response.data;
	}, function(error) {
		alert(error);
	});

}])

.controller('NewCommCtrl', ['$scope', '$q', 'commService', 'userService', function ($scope, $q, commService, userService) {

	$scope.newComm = {};

	userService.me(function(it) {
    	var commaIdx = it.user.location.name.indexOf(",");
		$scope.newComm.title = (commaIdx > -1) ?
			it.user.location.name.substring(0, commaIdx) : it.user.location.name;
		$scope.newComm.location = it.user.location.name;
	});

    $scope.submit = function() {
		commService.create({}, $scope.newComm, function(response) {
			window.location.href = '/#/comms/' + response.id;
		});
	};

}]);