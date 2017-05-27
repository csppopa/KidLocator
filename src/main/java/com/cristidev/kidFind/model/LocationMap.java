package com.cristidev.kidFind.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class LocationMap {

	public final List<Picture> pictures = new ArrayList<>();
	
	public void addPicture (Picture picture){
		pictures.add(picture);
	}

}
