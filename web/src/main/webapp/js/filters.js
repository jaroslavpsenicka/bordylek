angular.module('filters', [
])

.filter('capitalize', function() {
    return function(input, scope) {
        input = input.toLowerCase();
        return input.substring(0,1).toUpperCase() + input.substring(1);
    }
})

.filter('trusted', ['$sce', function($sce) {
    return function(text) {
        return $sce.trustAsHtml(text);
    };
}]);