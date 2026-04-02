package com.example.filemanagerservice.dto;

public class FileUploadResponse {

	private String fileName;
	private String url;
	private String message;

	public FileUploadResponse() {
	}

	public FileUploadResponse(String fileName, String url, String message) {
		this.fileName = fileName;
		this.url = url;
		this.message = message;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}