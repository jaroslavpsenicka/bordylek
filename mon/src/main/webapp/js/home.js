app.registerCtrl('HomeCtrl', function ($scope, $routeParams, $modal,
    chartsService, metricsService, alertsService, logsService) {

	var chartTemplate = {
        options: {
            legend: {
                enabled: false,
            },
            chart: {
                zoomType: 'x',
                width: 250,
                height: 150
            },
			credits: {
				enabled: false
			},
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
    $scope.logs = {};

	chartsService.get(function(response) {
	    for (var i = 0; i < response.length; i++) {
	        var chartId = response[i].id;
            $scope.charts[chartId] = angular.copy(chartTemplate);
            $scope.charts[chartId].id = chartId;
            $scope.charts[chartId].size = response[i].size || 'col-md-3';
            $scope.charts[chartId].title = {text: response[i].name};
            if ($scope.charts[chartId].size == 'col-md-6') {
                $scope.charts[chartId].options.chart.width = 545;
                $scope.charts[chartId].options.chart.height = 300;
            } else if ($scope.charts[chartId].size == 'col-md-9') {
                $scope.charts[chartId].options.chart.width = 835;
                $scope.charts[chartId].options.chart.height = 450;
            }
            $scope.loadChartData('gauge', chartId, response[i].serie, function(id, name, data) {
                $scope.charts[id].series = [{name: name, data: data}];
            });
	    }
	});

    $scope.loadChartData = function(type, id, name, callback) {
        metricsService.data({type: type, name: name}, function(response, request) {
            var chartData = [];
            for (var j = 0; j < response.data.length; j++) {
                chartData.push([response.data[j].timestamp, response.data[j].value]);
            }
            if (chartData.length > 0) {
                callback(id, name, chartData);
            }
        });
    };

    alertsService.get(function(response) {
        $scope.alerts = response;
    });

    $scope.closeChart = function(chartId) {
        chartsService.delete({id: chartId}, function(response) {
            delete $scope.charts[chartId];
        });
    };

    $scope.showLog = function(logId) {
        if ($scope.logs[logId]) delete $scope.logs[logId];
        else logsService.get({id: logId}, function(response) {
            $scope.logs[logId] = response.data;
        });
    };

    $scope.resolveAlert = function(alertId) {
        alertsService.resolveAlert({id: alertId}, function(response) {
            alertsService.get(function(response) {
                $scope.alerts = response;
            });
        });
    };

});
