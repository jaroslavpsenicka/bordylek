app.registerCtrl('HomeCtrl', ['$scope', '$routeParams', '$http', function ($scope, $routeParams, $http) {

	$scope.memoryChartConfig = {
        options: {
            chart: {
                type: 'column'
            }
        },
        series: [{
            data: [10, 15, 12, 8, 7]
        }],
        title: {
            text: 'Memory'
        },

        loading: false
    }

}]);
