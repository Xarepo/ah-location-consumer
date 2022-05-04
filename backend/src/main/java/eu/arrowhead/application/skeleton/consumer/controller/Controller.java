package eu.arrowhead.application.skeleton.consumer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import eu.arrowhead.application.skeleton.consumer.Domain.Coordinates;
import eu.arrowhead.application.skeleton.consumer.service.LocationService;

@RestController
public class Controller {
	
	//=================================================================================================
	// members

	@Autowired
	private LocationService locationService;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@GetMapping(path = "/location")
	public Coordinates echoService() {
		return locationService.fetchLocation();
	}

}
