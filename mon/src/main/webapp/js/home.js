app.registerCtrl('HomeCtrl', ['$scope', '$routeParams', 'metricsService', function ($scope, $routeParams, metricsService) {

    $scope.chartData = {};
    $scope.loadChartData = function(title, type, name, callback) {
        metricsService.data({type: type, name: name}, function(response) {
        	chartData = [];
        	for (var i = 0; i < response.data.length; i++) {
				var val = response.data[i];
				chartData.push([val.timestamp, val.value]);
        	}

        	callback(title, chartData);
        });
    }

    $scope.loadChartData('Memory', 'gauge', 'memory.heap.committed', function(title, data) {
		$scope.memoryChartConfig = angular.copy($scope.chartTemplate);
		$scope.memoryChartConfig.series.push({data: data});
		$scope.memoryChartConfig.title.text = title;
    });

	$scope.chartTemplate = {
        options: {
            legend: {
                enabled: false,
            },
            chart: {
                type: 'area',
                zoomType: 'x'
            },
			credits: {
				enabled: false
			},
            plotOptions: {
                area: {
                    fillColor: {
                        linearGradient: {
                            x1: 0,
                            y1: 0,
                            x2: 0,
                            y2: 1
                        },
                        stops: [
                            [0, Highcharts.getOptions().colors[0]],
                            [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                        ]
                    },
                    marker: {
                        radius: 2
                    },
                    lineWidth: 1,
                    states: {
                        hover: {
                            lineWidth: 1
                        }
                    },
                    threshold: null
                }
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
				}
			}
        }],
        series: [],
        title: {},
        loading: false
    };

}]);
