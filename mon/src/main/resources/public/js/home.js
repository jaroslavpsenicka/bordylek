app.registerCtrl('HomeCtrl', ['$scope', '$routeParams', 'metricsService', function ($scope, $routeParams, metricsService) {

    $scope.chartData = {};
    $scope.loadChartData = function(type, names, callback) {
        for (var i = 0; i < names.length; i++) {
            metricsService.data({type: type, name: names[i]}, function(response, request) {
                var chartData = [];
                for (var j = 0; j < response.data.length; j++) {
                    chartData.push([response.data[j].timestamp, response.data[j].value]);
                }
                if (chartData.length > 0) callback([{name: response.data[0].name, data: chartData}]);
            });
        }
    }

	$scope.chartTemplate = {
	    series: [],
        options: {
            legend: {
                enabled: false,
            },
            chart: {
                zoomType: 'x'
            },
			credits: {
				enabled: false
			}
        },
        xAxis: [{
            type: 'datetime'
        }],
        yAxis: [{
            title: {
                text: ''
            },
			labels: {
				formatter: function () {
                    var max = this.axis.getExtremes().max;
                    if (max > 999999999) {
                        return Math.round(this.value/1000000000) + 'G';
                    } else if (max > 999999) {
                        return Math.round(this.value/1000000) + 'M';
                    } else if (max > 999) {
                        return Math.round(this.value/1000) + 'k';
                    }

                    return this.value;
				}
			}
        }]
    };

    $scope.memoryChartConfig = angular.copy($scope.chartTemplate);
    $scope.memoryChartConfig.title = {text: 'Memory'};
    $scope.loadChartData('gauge', ['memory.heap.committed', 'memory.pools.PS-Old-Gen.used'], function(serie) {
		$scope.memoryChartConfig.series = $scope.memoryChartConfig.series.concat(serie);
    });

    $scope.threadsChartConfig = angular.copy($scope.chartTemplate);
    $scope.threadsChartConfig.title = {text: 'Threads'};
    $scope.loadChartData('gauge', ['threads.runnable.count', 'threads.waiting.count', 'threads.blocked.count',
        'threads.deadlock.count'], function(serie) {
		$scope.threadsChartConfig.series = $scope.threadsChartConfig.series.concat(serie);
    });

}]);
