package net.petrvopenka.geores.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.petrvopenka.geores.service.SearchService;

@RestController
public class GeoresController {
	
	@Autowired
	SearchService searchService;
	
	@RequestMapping("/search")
	public String search(@RequestParam(value="q", defaultValue="") String q){
		
		boolean transform2LatLong = true;
		
		String geoJSON = searchService.search(q, transform2LatLong);
		return geoJSON;
	}
	
	@RequestMapping("/search/street")
	public String searchStreet(@RequestParam(value="q", defaultValue="") String q){
		
		String geoJSON = searchService.searchStreet(q);
		return geoJSON;
	}
}
