package com.cristidev.kidFind.service;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Service;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class FacesService {

	protected static final String API_KEY = "e218cbcffde843e4a2a9a19b25099215";

	protected static final String API_SEC = "5c0f97545b2d4421ac6bcd7c2cddf72a";

	protected static final String DETECT_URL = "http://api.skybiometry.com/fc/faces/detect.json?";

	protected static final String RECOGNIZE_URL = "http://api.skybiometry.com/fc/faces/recognize.json?";

	protected static final String SAVE_URL = "http://api.skybiometry.com/fc/tags/save.json?";

	private final OkHttpClient client = new OkHttpClient();

	public Response sendDetectRequest(File sourcePhoto) {

		RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart("api_key", API_KEY).addFormDataPart("api_secret", API_SEC)
				.addFormDataPart("attributes", "all").addFormDataPart("file", sourcePhoto.getName(),
						RequestBody.create(MediaType.parse("image"), sourcePhoto))
				.build();

		Request request = new Request.Builder().url(DETECT_URL).method("POST", RequestBody.create(null, new byte[0]))
				.post(requestBody).build();

		Response response = null;
		try {
			response = client.newCall(request).execute();

		} catch (IOException e) {

			e.printStackTrace();
			return null;
		}

		return response;
	}

}
