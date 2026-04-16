package com.example.filemanagerservice.service;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.filemanagerservice.dto.UploadEmailRequest;

@Service
public class FileStorageService {

	private final AmazonS3 amazonS3;

	@Value("${S3_BUCKET}")
	private String bucketName;

	@Value("${EMAILER_SERVICE_URL}")
	private String emailerServiceUrl;

	public FileStorageService(AmazonS3 amazonS3) {
		this.amazonS3 = amazonS3;
	}

	public String uploadFile(MultipartFile file) {
		try {
			String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(file.getSize());
			metadata.setContentType(file.getContentType());

			InputStream inputStream = file.getInputStream();

			PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream, metadata);

			amazonS3.putObject(putObjectRequest);

			String fileUrl = amazonS3.getUrl(bucketName, fileName).toString();

			sendUploadSuccessEmail(fileName, fileUrl);

			return fileUrl;

		} catch (Exception e) {
			throw new RuntimeException("Upload failed", e);
		}
	}

	private void sendUploadSuccessEmail(String fileName, String fileUrl) {
		try {
			UploadEmailRequest request = new UploadEmailRequest();
			request.setTo("tugusu3@gmail.com");
			request.setUsername("Tugusu");
			request.setFileName(fileName);
			request.setFileUrl(fileUrl);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<UploadEmailRequest> entity = new HttpEntity<>(request, headers);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.postForEntity(emailerServiceUrl + "/send-upload-success", entity, String.class);
		} catch (Exception e) {
			System.out.println("Failed to send upload success email: " + e.getMessage());
		}
	}
}