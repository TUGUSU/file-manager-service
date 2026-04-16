package com.example.filemanagerservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.filemanagerservice.dto.FileUploadResponse;
import com.example.filemanagerservice.dto.NotifyUploadRequest;
import com.example.filemanagerservice.dto.PresignedUrlRequest;
import com.example.filemanagerservice.dto.PresignedUrlResponse;
import com.example.filemanagerservice.service.FileStorageService;
import com.example.filemanagerservice.service.SoapTokenValidationService;

@RestController
@RequestMapping("/files")
public class FileController {

	private final FileStorageService fileStorageService;
	private final SoapTokenValidationService soapTokenValidationService;

	public FileController(FileStorageService fileStorageService,
			SoapTokenValidationService soapTokenValidationService) {
		this.fileStorageService = fileStorageService;
		this.soapTokenValidationService = soapTokenValidationService;
	}

	@PostMapping("/upload")
	public ResponseEntity<?> uploadFile(@RequestHeader(value = "Authorization", required = false) String authHeader,
			@RequestParam("file") MultipartFile file) {

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return new ResponseEntity<>(new FileUploadResponse(null, null, "Missing or invalid Authorization header."),
					HttpStatus.UNAUTHORIZED);
		}

		String token = authHeader.substring(7);

		if (!soapTokenValidationService.validateToken(token)) {
			return new ResponseEntity<>(new FileUploadResponse(null, null, "Invalid token."), HttpStatus.UNAUTHORIZED);
		}

		if (file.isEmpty()) {
			return new ResponseEntity<>(new FileUploadResponse(null, null, "File is empty."), HttpStatus.BAD_REQUEST);
		}

		try {
			String url = fileStorageService.uploadFile(file);
			String fileName = url.substring(url.lastIndexOf("/") + 1);

			return new ResponseEntity<>(new FileUploadResponse(fileName, url, "File uploaded successfully."),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new FileUploadResponse(null, null, "Failed to upload file."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/presigned-url")
	public ResponseEntity<?> generatePresignedUrl(
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@RequestBody PresignedUrlRequest request) {

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header.");
		}

		String token = authHeader.substring(7);

		if (!soapTokenValidationService.validateToken(token)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token.");
		}

		try {
			PresignedUrlResponse response = fileStorageService.generatePresignedUploadUrl(request);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate presigned URL.");
		}
	}

	@PostMapping("/notify-upload-success")
	public ResponseEntity<String> notifyUploadSuccess(@RequestHeader("Authorization") String authHeader,
			@RequestBody NotifyUploadRequest request) {

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return ResponseEntity.status(401).body("Unauthorized");
		}

		String token = authHeader.substring(7);

		if (!soapTokenValidationService.validateToken(token)) {
			return ResponseEntity.status(401).body("Invalid token");
		}

		fileStorageService.notifyUploadSuccess(request.getFileName(), request.getFileUrl());

		return ResponseEntity.ok("Notification sent");
	}
}