'use strict';

/* Controllers */

function AppCtrl($scope, $location, $rootScope) {
	
	$scope.q = "";
	$rootScope.title = "";
	$rootScope.subtitle = "";
	
	$rootScope.activePlaces = [];
	
	$scope.submit = function() {
		$location.path('/search').search({q:$scope.q});
	};
	
}

function SearchCtrl($scope, $rootScope, $location, $routeParams, Place, messages) {
	
	$rootScope.title = messages["ui.search.results"];
	$rootScope.subtitle = "";
	
	$scope.$watch("total", function() {
		$rootScope.subtitle = $scope.total + " " + messages["ui.search.hits"];
	});
	
	$scope.$watch(("places"), function() {
		$rootScope.activePlaces = $scope.places;
	});
	
	$scope.search = {
		offset: ($routeParams.offset) ? parseInt($routeParams.offset) : 0,
		limit: ($routeParams.limit) ? parseInt($routeParams.limit) : 10,
		q: ($routeParams.q) ? ($routeParams.q) : "" 
	};
	
	$scope.places = [];
	$scope.total = 0;
	
	$scope.page = function() {
		return $scope.search.offset / $scope.search.limit + 1;
	};
	
	$scope.totalPages = function() {
		return ($scope.total - ($scope.total % $scope.search.limit)) / $scope.search.limit + 1;
	};
	
	$scope.setLimit = function(limit) {
		$scope.search.limit = limit;
		$scope.search.offset = 0;
		$location.search($scope.search);
		$scope.submit();
	};
	
	$scope.prevPage = function() {
		if ($scope.page() > 1) {
			$scope.search.offset = $scope.search.offset - $scope.search.limit;
			$location.search($scope.search);
			$scope.submit();
		}
	};
	
	$scope.nextPage = function() {
		if ($scope.page() < $scope.totalPages()) {
			$scope.search.offset = $scope.search.offset + $scope.search.limit;
			$location.search($scope.search);
			$scope.submit();
		}
	};
	
	$scope.submit = function() {
		Place.query($scope.search, function(result) {
			$scope.places = result.result;
			if ($scope.total != result.total)
				$scope.total = result.total;
		});
	};
	
	$scope.$watch(function(){ return $location.search().q; }, function() {
		console.log("q", $location.search().q);
		$scope.search.q = $location.search().q;
		$scope.submit();
	});
	
	$scope.submit();

}


function PlaceCtrl($scope, $rootScope, $routeParams, Place, $http) {
	
	$scope.location = { confidence: 0 };
	$scope.link = { predicate: "owl:sameAs" };
	$scope.success = false;
	$scope.failure = null;
	
	if ($routeParams.id) {
		$scope.place = Place.get({
			id: $routeParams.id
		}, function(result) {
			if (result.parent) {
				$http.get(result.parent).success(function(result) {
					$scope.parent = result;
				});
			}
			Place.query({q: "relatedPlaces:" + $scope.place.gazId}, function(result) {
				$scope.relatedPlaces = result.result;
			});
			$rootScope.title = result.prefName.title,
			$rootScope.subtitle = result["@id"]	+ '<a data-toggle="modal" href="#copyUriModal"><i class="icon-share"></i></a>';
			$rootScope.activePlaces = [ result ];
		});
	}
	
	$scope.save = function() {
		Place.save(
			$scope.place,
			function(data) {
				if (data.gazId) {
					$scope.place = data;
				}
				$scope.success = true; 
				$scope.failure = null; 
				window.scrollTo(0,0);
			},
			function(result) {
				$scope.failure = result.data.message; 
				$scope.success = false; 
				window.scrollTo(0,0);
			}
		);
	};
	
	$scope.addComment = function() {
		if (!$scope.comment.text || !$scope.comment.language) return;
		if ($scope.place.comments == undefined)
			$scope.place.comments = [];
		$scope.place.comments.push($scope.comment);
		$scope.comment = {};
	};
	
	$scope.addName = function() {
		if (!$scope.name.title) return;
		if ($scope.place.names == undefined)
			$scope.place.names = [];
		$scope.place.names.push($scope.name);
		$scope.name = {};
	};
	
	$scope.addLocation = function() {
		if (!$scope.location.coordinates) return;
		if ($scope.place.locations == undefined)
			$scope.place.locations = [];
		$scope.place.locations.push($scope.location);
		$scope.location = { confidence: 0 };
	};
	
	$scope.addIdentifier = function() {
		if (!$scope.identifier.value || !$scope.identifier.context) return;
		if ($scope.place.identifiers == undefined)
			$scope.place.identifiers = [];
		$scope.place.identifiers.push($scope.identifier);
		$scope.identifier = { };
	};
	
	$scope.addLink = function() {
		if (!$scope.link.object || !$scope.link.predicate) return;
		if ($scope.place.links == undefined)
			$scope.place.links = [];
		$scope.place.links.push($scope.link);
		$scope.link = { predicate: "owl:sameAs" };
	};
	
	$scope.addRelatedPlace = function() {
		if (!$scope.relatedPlace['@id']) return;
		if ($scope.place.relatedPlaces == undefined)
			$scope.place.relatedPlaces = [];
		$scope.place.relatedPlaces.push($scope.relatedPlace['@id']);
		$scope.relatedPlace = {};
	};

}

function MergeCtrl($scope, $routeParams, Place, $http) {
	
	if ($routeParams.id) {
		$scope.place = Place.get({
			id: $routeParams.id
		}, function(result) {
			// TODO: more like this query
			Place.query({q: result.prefName.title, fuzzy: true}, function(result) {
				$scope.candidatePlaces = result.result;
			});
		});
	}
	
}
