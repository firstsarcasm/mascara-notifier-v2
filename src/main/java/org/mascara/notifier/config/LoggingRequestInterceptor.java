package org.mascara.notifier.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

	public static final String NEW_LINE = "\n";

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		logRequest(request, body);
		ClientHttpResponse response = execution.execute(request, body);
		logResponse(response);
		return response;
	}

	private void logRequest(HttpRequest request, byte[] body) {
		log.info(
				"""    			
				\n===========================request begin=============================================
				URI         : {}",
				Method      : {}",
				Headers     : {}",
				Request body: {}",
				===========================request end===============================================
				""",
				request.getURI(),
				request.getMethod(),
				request.getHeaders(),
				new String(body, StandardCharsets.UTF_8)
		);
	}

	private void logResponse(ClientHttpResponse response) throws IOException {
		String responseBody = getResponseBody(response);
		log.info(
				"""
				\n============================response begin==========================================
				Status code  : {}",
				Status text  : {}",
				Headers      : {}",
				Response body: {}",
				=======================response end=================================================
				""",
				response.getStatusCode(),
				response.getStatusText(),
				response.getHeaders(),
				responseBody
		);
	}

	private String getResponseBody(ClientHttpResponse response) throws IOException {
		InputStreamReader in = new InputStreamReader(response.getBody(), StandardCharsets.UTF_8);
		return new BufferedReader(in).lines()
				.collect(Collectors.joining(NEW_LINE));
	}

}
