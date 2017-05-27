package com.cristidev.kidFind.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cristidev.kidFind.service.FacesService;
import com.github.mhendred.face4j.exception.FaceClientException;
import com.github.mhendred.face4j.exception.FaceServerException;

@RestController
public class FacesController {
	
	@Autowired
	private FacesService service;

	@RequestMapping(value="recognize", method=RequestMethod.GET)
	public String recognizeFace() {
		
		try {
			return service.searchForPerson().toString();
		} catch (FaceClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FaceServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}

}
