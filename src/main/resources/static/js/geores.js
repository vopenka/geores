var map;
function initMap() {
	
	
	var map = new google.maps.Map(document.getElementById('map'), {
		center : {
			lat : 43.734400, 
			lng : 7.420327
		},
		zoom : 14,
		draggable: true,
		panControl: true,
		disableDefaultUI: true
	});
	
	
	var myPlaces;
	var bounds = new google.maps.LatLngBounds();
	
	
	// Add style to loaded points
	var stylePoint = {
		path : google.maps.SymbolPath.CIRCLE,
		scale : 4,
		fillColor : 'red',
		fillOpacity : 1,
		strokeWeight : 1
	}
	var stylePointSelected = {
		path : google.maps.SymbolPath.CIRCLE,
		scale : 4,
		fillColor : 'orange',
		fillOpacity : 1,
		strokeWeight : 1
	}
	map.data.setStyle(function(feature) {
		var currentStylePoint = stylePoint;
		if (feature.getProperty('selected')) {
			currentStylePoint = stylePointSelected;
		    }
		return ({
			icon : currentStylePoint
		});
	});
	
	
	$('#searchForm').on('submit', function (e) {
		e.preventDefault();
		
		console.log($(this).serialize());
		
		// remove all points from map
		map.data.forEach(function(feature){
			map.data.remove(feature);
		});
		
		// remove all items from list
		var locationList = document.getElementById("locationsList");
		while (locationsList.firstChild) {
			locationsList.removeChild(locationsList.firstChild);
		}

		$.ajax({
			  //url: "http://ec2-54-194-178-37.eu-west-1.compute.amazonaws.com:8081/search?" + $(this).serialize(),
			  url: "http://localhost:8081/search/street?" + $(this).serialize(),
			  type: 'GET'
			}).done(function(data) {

				map.data.addGeoJson(JSON.parse(data));
				
				var myPlaceListTable = document.createElement("table");
				var myPlaceListTableHead = document.createElement("thead");
				var myPlaceListTableHeadRow = document.createElement("tr");
				var myPlaceListTableHeadHeader = document.createElement("th");
				var myPlaceListTableHeadTitle = document.createTextNode('List of locations found through query');
				myPlaceListTableHead.appendChild(myPlaceListTableHeadRow);
				myPlaceListTableHeadRow.appendChild(myPlaceListTableHeadHeader);
				myPlaceListTableHeadHeader.appendChild(myPlaceListTableHeadTitle);
				
				var myPlaceListTableBody = document.createElement("tbody");
				myPlaceListTable.setAttribute("class", "table table-hover");
				document.getElementById("locationsList").appendChild(myPlaceListTable);
				myPlaceListTable.appendChild(myPlaceListTableHead);
				myPlaceListTable.appendChild(myPlaceListTableBody);

				map.data.forEach(function(feature) {
					bounds.extend(feature.getGeometry().get());

					//create my places list items and style them
					var myPlaceListItemRow = document.createElement("tr");
					var myPlaceListItemCell = document.createElement("td");
					var myPlaceListItemLink = document.createElement("a");
					var myPlaceListItemTitle = document.createTextNode(feature.getProperty('name'));
					
					//myPlaceListItem.setAttribute("style", "margin-top:10px;");
					//myPlaceListItemLink.setAttribute("class", "btn btn-default");
					myPlaceListItemRow.id = "myPlace-" + feature.getId();
					
					myPlaceListItemRow.appendChild(myPlaceListItemCell);
					myPlaceListItemRow.setAttribute("class", "default");
					myPlaceListItemCell.appendChild(myPlaceListItemLink);
					myPlaceListItemLink.appendChild(myPlaceListItemTitle);
					myPlaceListTableBody.appendChild(myPlaceListItemRow);
					
					myPlaceListItemRow.addEventListener("click", function(){
						selectMyPlaceFromList(feature.getId());
					});
					
				});

				// zoom to see all myPlaces
				map.fitBounds(bounds);

			});

	});
	
	// Select myPlace by clicking on an item in the list
	function selectMyPlaceFromList(id){
		selectMyPlace(id, 'list');
	}
	
	// Select myPlace by clicking in the map
	map.data.addListener('click', function(event) {
		selectMyPlace(event.feature.getId(), 'map');
	});
	
	function selectMyPlace(id, selectionType){
		map.data.forEach(function(feature) {
			if(feature.getId() != id){
				feature.setProperty('selected', undefined);
				if($('#myPlace-' + feature.getId()).hasClass("warning")){
					$('#myPlace-' + feature.getId()).removeClass("warning").addClass("default");
				}
			} else {
				feature.setProperty('selected', true);
				if($('#myPlace-' + feature.getId()).hasClass("default")){
					$('#myPlace-' + feature.getId()).removeClass("default").addClass("warning");
				}
				if(selectionType === 'list'){
					map.panTo(feature.getGeometry().get());
				}
			}
		});
	};

}