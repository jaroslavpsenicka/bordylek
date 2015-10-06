app.registerCtrl('DataCtrl', function ($scope, $routeParams, $location, dataService) {

    $scope.query = '';
	$scope.metadata = {
		'org.bordylek.service.model.blog.Article': {
			name: 'blog',
			fields: [{
				name: 'id',
				title: 'Id',
				type: 'text',
				required: true,
				locked: true
			}, {
				name: 'name',
				title: 'Name',
				type: 'text',
				required: true,
				list: true
			}, {
				name: 'createDate',
				title: 'Created',
				type: 'datetime',
				list: true
			}, {
				name: 'author',
				title: 'Author',
				type: 'User',
				list: true
			}]}
		},
		'org.bordylek.service.model.Community': {
			name: 'communities',
			fields: [{
				name: 'id',
				title: 'Id',
				type: 'text',
				required: true,
				locked: true
			}, {
				name: 'name',
				title: 'Name',
				type: 'text',
				required: true,
				list: true
			}, {
			    name: 'createDate',
				title: 'Created',
				type: 'datetime',
				list: true
			}, {
			    name: 'createdBy',
				title: 'By',
				type: 'User',
				list: true
			}]
		},
		'org.bordylek.service.model.User': {
			name: 'users',
			fields: [{
				name: 'id',
				title: 'Id',
				type: 'text',
				required: true,
				locked: true
			}, {
				name: 'name',
				title: 'Name',
				type: 'text',
				required: true,
				list: true
			}, {
			    name: 'email',
				title: 'Email',
				type: 'text',
				required: true,
				list: true
			}, {
			    name: 'status',
				title: 'Status',
				type: 'text',
				required: true,
                list: true
			}, {
			    name: 'regId',
				title: 'Registrar ID',
				type: 'text',
				required: true,
                list: true
			}]
		}
	};

	$scope.selectClass = function(entityClass) {
		$scope.selectedClass = entityClass;
		$scope.selectedClassName = $scope.metadata[entityClass].name;
		$scope.selectedClassFields = $scope.metadata[entityClass].fields;
	}

    $scope.findData = function() {
        dataService.list({class: $scope.selectedClass, query: $scope.query}, function(result) {
            $scope.data = result;
        });
    }

    $scope.save = function() {
        dataService.save({class: $scope.selectedClass}, $scope.selectedEntity, function(result) {
            $location.path('/data');
        });
    }

    $scope.remove = function(id) {
        dataService.remove({id: id}, function(result) {
            $scope.findData();
        });
    }

	$scope.selectClass('org.bordylek.service.model.User');

	if ($routeParams.id) {
		dataService.load({class: $scope.selectedClass, id: $routeParams.id}, function(result) {
			$scope.selectedEntity = result;
		});
	}

	$scope.unlockedFields = {};

	$scope.unlock = function(fieldName) {
		$scope.unlockedFields[fieldName] = 1;
	}
});
