app.registerCtrl('DataCtrl', function ($scope, dataService) {

    $scope.query = '';
	$scope.metadata = {
		'org.bordylek.service.model.blog.Article': {
			'name': 'blog',
			'fields': {
			    'name': {
			        'title': 'Name',
			        'type': 'String'
			    },
			    'createDate': {
			        'title': 'Created',
			        'type': 'Date'
			    },
			    'author': {
			        'title': 'Author',
			        'type': 'User'
			    }
			}
		},
		'org.bordylek.service.model.Community': {
			'name': 'communities',
			'fields': {
			    'name': {
			        'title': 'Name',
			        'type': 'String'
			    },
			    'createDate': {
			        'title': 'Created',
			        'type': 'Date'
			    },
			    'createdBy': {
			        'title': 'By',
			        'type': 'User'
			    }
			}
		},
		'org.bordylek.service.model.User': {
			'name': 'users',
			'fields': {
			    'name': {
			        'title': 'Name',
			        'type': 'String'
			    },
			    'email': {
			        'title': 'Email',
			        'type': 'String'
			    },
			    'status': {
			        'title': 'Status',
			        'type': 'String'
			    },
			    'location': {
                    'title': 'Location',
                    'type': 'String'
			    }
			}
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

    $scope.remove = function(id) {
        dataService.remove({id: id}, function(result) {
            $scope.findData();
        });
    }

	$scope.selectClass('org.bordylek.service.model.User');

});
