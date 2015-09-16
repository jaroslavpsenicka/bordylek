app.registerCtrl('HomeCtrl', ['$scope', '$routeParams', '$http', function ($scope, $routeParams, $http) {

	$scope.memoryChartConfig = {
        options: {
            legend: {
                enabled: false,
            },
            chart: {
                type: 'area',
                zoomType: 'x'
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
            }
        }],
        series: [{
            data: [[1323453847844,0.8873],
                   [Date.UTC(2015,5,14),0.8913],
                   [Date.UTC(2015,5,15),0.8862],
                   [Date.UTC(2015,5,16),0.8891],
                   [Date.UTC(2015,5,17),0.8821],
                   [Date.UTC(2015,5,18),0.8802],
                   [Date.UTC(2015,5,19),0.8808],
                   [Date.UTC(2015,5,21),0.8794],
                   [Date.UTC(2015,5,22),0.8818],
                   [Date.UTC(2015,5,23),0.8952],
                   [Date.UTC(2015,5,24),0.8924],
                   [Date.UTC(2015,5,25),0.8925],
                   [Date.UTC(2015,5,26),0.8955],
                   [Date.UTC(2015,5,28),0.9113],
                   [Date.UTC(2015,5,29),0.8900],
                   [Date.UTC(2015,5,30),0.8950]
                   ]
        }],
        title: {
            text: 'Memory'
        },

        loading: false
    }

}]);
