package com.cristidev.kidFind.controller;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import com.cristidev.kidFind.service.FacesService;
import com.cristidev.kidFind.service.StorageService;

import okhttp3.Response;

@RestController
public class ParentPhotoUploadController {

	@Autowired
	@Qualifier("parent")
	private StorageService storageService;

	@Autowired
	private FacesService facesService;

	@GetMapping("/parent")
	public String listUploadedFiles(Model model) throws IOException {

		model.addAttribute("files", storageService.loadAll()
				.map(path -> MvcUriComponentsBuilder
						.fromMethodName(ParentPhotoUploadController.class, "serveFile", path.getFileName().toString())
						.build().toString())
				.collect(Collectors.toList()));

		return "uploadForm";
	}

	@GetMapping("/parent/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		Resource file = storageService.loadAsResource(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}

	@PostMapping(value = "/parent", produces="text/html")
	@ResponseBody
	public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) throws IOException, JSONException {
		storageService.store(file);
		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + file.getOriginalFilename() + "!");
		
		Coordinates resultCoordinates = facesService.searchFaceRequest(new File("upload-dir/parentPhoto.jpg"));

		System.out.println(resultCoordinates);
		JSONObject cookie = null;
		if (resultCoordinates != null) {
			cookie = new JSONObject();
			cookie.put("x", resultCoordinates.x);
			cookie.put("y", resultCoordinates.y);
		}

		String redirect = "<html><head><script>window.location.href='showMap.html'</script></head></html>";

		ResponseEntity.BodyBuilder response = ResponseEntity
				.status(HttpStatus.OK);
		if (cookie != null) {
				response.header("Set-Cookie", "coordinates=" + cookie.toString());
		}

		return response.body(redirect);
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}
