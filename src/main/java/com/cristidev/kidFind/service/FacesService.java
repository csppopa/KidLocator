package com.cristidev.kidFind.service;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cristidev.kidFind.model.Coordinates;
import com.cristidev.kidFind.model.LocationMap;
import com.cristidev.kidFind.model.Picture;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class FacesService {

	@Autowired
	private LocationMap locationMap;

	protected static final String API_KEY = "e218cbcffde843e4a2a9a19b25099215";

	protected static final String API_SEC = "5c0f97545b2d4421ac6bcd7c2cddf72a";

	protected static final String DETECT_URL = "http://api.skybiometry.com/fc/faces/detect.json?";

	protected static final String RECOGNIZE_URL = "http://api.skybiometry.com/fc/faces/recognize.json?";

	protected static final String SAVE_TAG_URL = "http://api.skybiometry.com/fc/tags/save.json?";

	protected static final String NAMESPACE = "@workws";

	private final OkHttpClient client = new OkHttpClient();

	private static final char[] ALPHABET = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
			'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	public Coordinates searchFaceRequest(File sourcePhoto) {

		RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart("api_key", API_KEY).addFormDataPart("api_secret", API_SEC)
				.addFormDataPart("attributes", "all").addFormDataPart("file", sourcePhoto.getName(),
						RequestBody.create(MediaType.parse("image"), sourcePhoto))
				.build();

		Request request = new Request.Builder().url(DETECT_URL).method("POST", RequestBody.create(null, new byte[0]))
				.post(requestBody).build();

		try {
			Response response = client.newCall(request).execute();
			JSONObject json = new JSONObject(response.body().string());
			JSONArray photos = json.getJSONArray("photos");
			String tid = photos.getJSONObject(0).getJSONArray("tags").getJSONObject(0).getString("tid");
			String uid = generateUid(8);
			Request secondRequest = generateSecondRequest(uid, tid);
			Response secondResponse = client.newCall(secondRequest).execute();
			JSONObject secondJson = new JSONObject(secondResponse.body().string());
			if (!secondJson.getString("status").equals("succes")) {
				throw new IOException("tag save failed");
			}
			for (Picture picture : locationMap.pictures) {
				Request thirdRequest = generateThirdRequest(uid, picture.filePath);
				Response newResponse = client.newCall(thirdRequest).execute();
				JSONObject recognitionResult = new JSONObject(response.body().string());
				if (!recognitionResult.getString("status").equals("succes")) {
					System.out.println("recognition failed for: " + picture.filePath);
					break;
				}
				if (recognitionResult.getJSONArray("photos").getJSONObject(0).has("tags")) {
					return picture.coordinates;
				}
			}

		} catch (IOException | JSONException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		return null;
	}

	private Request generateThirdRequest(String uid, String filePath) {
		File photo = new File(filePath);
		RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart("api_key", API_KEY).addFormDataPart("api_secret", API_SEC)
				.addFormDataPart("attributes", "all")
				.addFormDataPart("file", photo.getName(), RequestBody.create(MediaType.parse("image"), photo)).build();
		Request request = new Request.Builder().url(RECOGNIZE_URL).method("POST", RequestBody.create(null, new byte[0]))
				.post(requestBody).build();
		return request;
	}

	private Request generateSecondRequest(String uid, String tid) {
		RequestBody requestBody = new FormBody.Builder().add("api_key", API_KEY).add("api_sec", API_SEC)
				.add("uid", uid + NAMESPACE).add("tids", tid).build();

		Request request = new Request.Builder().url(DETECT_URL).method("POST", RequestBody.create(null, new byte[0]))
				.post(requestBody).build();

		return request;
	}

	private String generateUid(int length) {
		StringBuilder name = new StringBuilder();
		for (int index = 0; index < length; index++) {
			name.append(ALPHABET[(int) Math.floor(Math.random() * ALPHABET.length)]);
		}
		return name.toString();
	}

}
