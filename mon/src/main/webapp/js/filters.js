angular.module('filters', [])

.filter('daygroup', function() {

    var today = new Date();
    today.setHours(0,0,0,0);
    var yesterday = new Date();
    yesterday.setHours(0,0,0,0);
    yesterday.setDate(yesterday.getDate()-1);

    return function(input, scope) {
        for (var i = 0; i < input.length; i++) {
            input[i].daygroup = 'today';
            if (input[i].timestamp < today.getTime()) {
                input[i].daygroup = 'yesterday';
            }
            if (input[i].timestamp < yesterday.getTime()) {
                input[i].daygroup = 'earlier';
            }
        }

        return input;
    }
});