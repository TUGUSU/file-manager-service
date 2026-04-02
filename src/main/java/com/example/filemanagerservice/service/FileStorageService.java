package com.example.filemanagerservice.service;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Service
public class FileStorageService {

	private final AmazonS3 amazonS3;

	@Value("${spaces.bucket}")
	private String bucketName;

	public FileStorageService(AmazonS3 amazonS3) {
		this.amazonS3 = amazonS3;
	}

	public String uploadFile(MultipartFile file) throws IOException {
		String originalFileName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
		String fileName = System.currentTimeMillis() + "_" + originalFileName.replaceAll("\\s+", "_");

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(file.getSize());
		metadata.setContentType(file.getContentType());

		try (InputStream inputStream = file.getInputStream()) {
			PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream, metadata)
					.withCannedAcl(CannedAccessControlList.PublicRead);

			amazonS3.putObject(putObjectRequest);
		}

		return amazonS3.getUrl(bucketName, fileName).toString();
	}
}