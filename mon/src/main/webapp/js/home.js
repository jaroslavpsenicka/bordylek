app.registerCtrl('HomeCtrl', function ($scope, $routeParams, $modal, chartsService, metricsService, alertsService) {

	var chartTemplate = {
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

    $scope.charts = {};
	chartsService.get(function(response) {
	    for (var i = 0; i < response.length; i++) {
	        var chartId = response[i].id;
            $scope.charts[chartId] = angular.copy(chartTemplate);
            $scope.charts[chartId].title = {text: response[i].name};
            $scope.loadChartData('gauge', response[i].serie, function(serie) {
                $scope.charts[chartId].series = serie;
            });
	    }
	});

    $scope.loadChartData = function(type, name, callback) {
        metricsService.data({type: type, name: name}, function(response, request) {
            var chartData = [];
            for (var j = 0; j < response.data.length; j++) {
                chartData.push([response.data[j].timestamp, response.data[j].value]);
            }
            if (chartData.length > 0) callback([{name: response.data[0].name, data: chartData}]);
        });
    };

    alertsService.get(function(response) {
        $scope.alerts = response;
    });

});
