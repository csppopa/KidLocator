package com.cristidev.kidFind.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cristidev.kidFind.service.FacesService;

@RestController
public class FacesController {
	
	@Autowired
	private FacesService service;

	@RequestMapping(value="recognize", method=RequestMethod.GET)
	public String recognizeFace() {
		
		return null;
		
	}

}
