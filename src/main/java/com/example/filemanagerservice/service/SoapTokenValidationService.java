package com.example.filemanagerservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SoapTokenValidationService {

	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${soap.service.url}")
	private String soapServiceUrl;

	public boolean validateToken(String token) {
		try {
			String soapRequest = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
					+ "xmlns:aut=\"http://example.com/auth\">" + "<soapenv:Header/>" + "<soapenv:Body>"
					+ "<aut:ValidateTokenRequest>" + "<aut:token>" + token + "</aut:token>"
					+ "</aut:ValidateTokenRequest>" + "</soapenv:Body>" + "</soapenv:Envelope>";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_XML);

			HttpEntity<String> requestEntity = new HttpEntity<>(soapRequest, headers);

			ResponseEntity<String> response = restTemplate.exchange(soapServiceUrl, HttpMethod.POST, requestEntity,
					String.class);

			String responseBody = response.getBody();

			return responseBody != null && (responseBody.contains("<ns2:valid>true</ns2:valid>")
					|| responseBody.contains("<ns3:valid>true</ns3:valid>")
					|| responseBody.contains("<valid>true</valid>"));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}