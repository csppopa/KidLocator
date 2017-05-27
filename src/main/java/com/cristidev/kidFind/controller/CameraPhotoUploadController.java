package com.cristidev.kidFind.controller;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

import javax.websocket.server.PathParam;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cristidev.kidFind.exception.StorageFileNotFoundException;
import com.cristidev.kidFind.model.Coordinates;
import com.cristidev.kidFind.model.LocationMap;
import com.cristidev.kidFind.model.Picture;
import com.cristidev.kidFind.service.StorageService;

import okhttp3.Request;
import okhttp3.RequestBody;

@RestController
public class CameraPhotoUploadController {

	private static final String NOTIFICATION_TOPIC = "/topics/hackTmKidLocator";
	private static final String NOTIFICATION_URL = "https://fcm.googleapis.com/fcm/send";
	private static final String ANDROID_NOTIFICATIONS_API_KEY = "key= AIzaSyAIKs2qVYf8wWGOZlWCH9dZK0lXy9SeFyI";
	private final OkHttpClient client = new OkHttpClient();
	
	@Autowired
	@Qualifier("camera")
	private StorageService storageService;

	@Autowired
	private LocationMap locationMap;

	@GetMapping("/camera")
	public String listUploadedFiles(Model model) throws IOException {

		model.addAttribute("files", storageService.loadAll()
				.map(path -> MvcUriComponentsBuilder
						.fromMethodName(ParentPhotoUploadController.class, "serveFile", path.getFileName().toString())
						.build().toString())
				.collect(Collectors.toList()));

		return "uploadForm";
	}

	@GetMapping("/camera/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		Resource file = storageService.loadAsResource(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}

	@PostMapping("/camera/{x}/{y}")
	public void handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes,
			@PathParam(value = "x") String x, @PathParam(value = "y") String y) {

		storageService.store(file);
		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + file.getOriginalFilename() + "!");

		locationMap.addPicture(new Picture(new Coordinates(Double.valueOf(x), Double.valueOf(y)),
				new File("upload-dir/" + file.getOriginalFilename()).getAbsolutePath()));
	}
	
	@PostMapping("/cameras/notify")
	public void notifyCameras() throws JSONException, IOException {
		JSONObject request = new JSONObject();
		request.put("to", NOTIFICATION_TOPIC);
		request.put("data", new JSONObject());
		request.getJSONObject("data").put("message", "vasile");
		
		Request httpRequest = new Request.Builder()
				.url(NOTIFICATION_URL)
				.header("Authorization", ANDROID_NOTIFICATIONS_API_KEY)
				.header("Content-Type", "application/json")
				.method("POST", RequestBody.create(MediaType.parse("application/json"), request.toString()))
				.build();
		client.newCall(httpRequest).execute();
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<Void> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}
