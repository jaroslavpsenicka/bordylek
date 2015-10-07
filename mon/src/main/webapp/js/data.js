app.registerCtrl('DataCtrl', function ($scope, $rootScope, $routeParams, $location, dataService) {

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
				name: 'title',
				title: 'Title',
				type: 'text',
				required: true,
				list: true
			}, {
				name: 'resume',
				title: 'Resume',
				type: 'text',
				required: true
			}, {
				name: 'text',
				title: 'Text',
				type: 'html',
				required: true
			}, {
				name: 'createDate',
				title: 'Created',
				type: 'datetime-local',
				list: true
			}, {
				name: 'valid',
				title: 'Valid',
				type: 'boolean'
			}, {
				name: 'author',
				title: 'Author',
				type: 'User'
			}, {
				name: 'category',
				title: 'Category',
				type: 'Category'
			}]
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
		$rootScope.selectedClass = entityClass;
		$rootScope.selectedClassName = $scope.metadata[entityClass].name;
		$scope.selectedClassFields = $scope.metadata[entityClass].fields;
	}

    $scope.findData = function() {
        dataService.list({class: $scope.selectedClass, query: $scope.query}, function(result) {
            $scope.data = result;
        });
    }

    $scope.save = function() {
        dataService.save({class: $scope.selectedClass}, $scope.selectedEntity, function(result) {
            $location.path('/data').search({ class: $scope.selectedClass });
        });
    }

    $scope.remove = function(id) {
        dataService.remove({id: id}, function(result) {
            $scope.findData();
        });
    }

	if ($routeParams.class) {
		$scope.selectClass($routeParams.class);
		if ($routeParams.id) {
    		dataService.load({class: $routeParams.class, id: $routeParams.id}, function(result) {
    			$scope.selectedEntity = result;
    		});
		}
	} else {
    	$scope.selectClass('org.bordylek.service.model.User');
	}

	$scope.unlockedFields = {};

	$scope.unlock = function(fieldName) {
		$scope.unlockedFields[fieldName] = 1;
	}
});
