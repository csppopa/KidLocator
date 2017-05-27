package com.cristidev.kidFind.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.github.mhendred.face4j.DefaultFaceClient;
import com.github.mhendred.face4j.FaceClient;
import com.github.mhendred.face4j.exception.FaceClientException;
import com.github.mhendred.face4j.exception.FaceServerException;
import com.github.mhendred.face4j.model.Face;
import com.github.mhendred.face4j.model.Guess;
import com.github.mhendred.face4j.model.Photo;

@Service
public class FacesService {

	protected static final String API_KEY = "e218cbcffde843e4a2a9a19b25099215";
	
	protected static final String API_SEC = "5c0f97545b2d4421ac6bcd7c2cddf72a";

	private static final String USER_ID = "person11";

	public List<List<Guess>> searchForPerson() throws FaceClientException, FaceServerException{
		
		FaceClient faceClient = new DefaultFaceClient(API_KEY, API_SEC);
    	
    	/**
    	 * First we detect some faces in a url. This URL has a single face, So we get back one
    	 * Photo object with one Face object in it.
    	 * 
    	 * You can pass more than one URL (comma delimited String) or you can pass an image file    
    	 * 
    	 * @see https://www.skybiometry.com/Documentation#faces/detect
    	 * @see https://www.skybiometry.com/Documentation#faces/recognize
    	 */
    	Photo photo = faceClient.detect(new File("upload-dir/uploaded.jpg"));
    
    	/*
    	 * Now we pull out the temporary tag and call save with the desired username and label.
    	 */
    	Face f = photo.getFace();
    	faceClient.saveTags(f.getTID(), USER_ID, "a label");
    	
    	/*
    	 * Let get the training status for this user now. We should see training in progress TRUE
    	 * because we havent called train yet.
    	 */
    	faceClient.status(USER_ID);
    	
    
    	/*
    	 * IMPORTANT: Now we call train on our untrained user. This will commit our saved tag for this user to
    	 * the database so we can recognize them later with 'recognize' calls   
    	 */
    	faceClient.train(USER_ID);
    	
    	/**
    	 * Now we can call recognize. Look for any user in our index (we only have one now)
    	 * We should see a guess now
    	 */
    	photo = faceClient.recognize(new File("existing-dir/group.jpg"), "person");
    	
    	List<List<Guess>> returned = new ArrayList<List<Guess>>();
    	
    	for (Face face : photo.getFaces())
    	{
    		returned.add(face.getGuesses());
    	}
    	
    	return returned;
	}
}
