app.registerCtrl('RulesCtrl', function ($scope, rulesService) {

	rulesService.readRules(function(response) {
		$scope.rules = response.data;
	});

	$scope.onChange = function(rule) {
		rule.disabled = true;
		rulesService.toggleRule({}, rule.packageName + '.' + rule.name, function(response) {
			rule.disabled = false;
			rule.enabled = response.enabled;
		}, function(error) {
			rule.disabled = false;
			rule.enabled = !rule.enabled;
		});
	}

});
