app.registerCtrl('HomeCtrl', function ($scope, $routeParams, $modal,
    chartsService, metricsService, alertsService, logsService) {

	var chartTemplate = {
        options: {
            legend: {
                enabled: false,
            },
            chart: {
                type: 'line',
                zoomType: 'x'
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
    $scope.shownLogs = {};

	chartsService.get(function(response) {
	    for (var i = 0; i < response.length; i++) {
	        var chartId = response[i].id;
	        var chartInfo = $scope.decodeName(response[i].serie);
            $scope.charts[chartId] = angular.copy(chartTemplate);
            $scope.charts[chartId].id = chartId;
            $scope.charts[chartId].size = response[i].size || 'col-md-3';
            $scope.charts[chartId].title = {text: response[i].name};
            $scope.configureSize($scope.charts[chartId]);
            $scope.loadChartData(chartId, chartInfo, function(id, name, data) {
                $scope.charts[id].series = [{name: name, data: data}];
            });
	    }
	});

    $scope.loadChartData = function(id, info, callback) {
        metricsService.data({type: info.type, name: info.name}, function(response, request) {
            var chartData = [];
            for (var j = 0; j < response.data.length; j++) {
                chartData.push([response.data[j].timestamp, response.data[j][info.attr]]);
            }
            if (chartData.length > 0) {
                callback(id, info.name, chartData);
            }
        });
    };

    $scope.decodeName = function(name) {
        var decoded = {type: 'gauge', name: name, attr: 'value'};
        if (decoded.name && decoded.name.indexOf(':') > -1) {
            var parts = decoded.name.split(':');
            decoded.type = parts[0];
            decoded.name = parts[1];
        }

        if (decoded.name && decoded.name.indexOf('/') > -1) {
            var parts = decoded.name.split('/');
            decoded.name = parts[0];
            decoded.attr = parts[1];
        }

        return decoded;
    }

    $scope.configureSize = function(chart) {
        if (chart.size == 'col-md-6') {
            chart.options.chart.width = 545;
            chart.options.chart.height = 300;
        } else if (chart.size == 'col-md-9') {
            chart.options.chart.width = 835;
            chart.options.chart.height = 450;
        } else {
            chart.options.chart.width = 250;
            chart.options.chart.height = 150;
        }
    }

    alertsService.get(function(response) {
        $scope.alerts = response;
    });

    $scope.closeChart = function(chartId) {
        chartsService.delete({id: chartId}, function(response) {
            delete $scope.charts[chartId];
        });
    };

    $scope.showLog = function(alertId) {
        if ($scope.shownLogs[alertId]) delete $scope.shownLogs[alertId];
        else $scope.shownLogs[alertId] = true;
    };

    $scope.resolveAlert = function(alertId) {
        alertsService.resolveAlert({id: alertId}, function(response) {
            alertsService.get(function(response) {
                $scope.alerts = response;
            });
        });
    };

});
