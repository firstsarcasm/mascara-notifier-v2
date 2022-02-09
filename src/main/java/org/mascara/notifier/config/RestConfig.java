package org.mascara.notifier.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class RestConfig {

	@Bean
	@Primary
	public ObjectMapper customObjectMapper() {
		return new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
				.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	@Bean
	public RestTemplate customRestTemplate(LoggingRequestInterceptor loggingRequestInterceptor) {
		ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
		RestTemplate restTemplate = new RestTemplate(factory);

		MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
		messageConverter.setPrettyPrint(false);
		messageConverter.setObjectMapper(customObjectMapper());
		restTemplate.getMessageConverters().removeIf(m -> m.getClass().getName().equals(MappingJackson2HttpMessageConverter.class.getName()));
		restTemplate.getMessageConverters().add(messageConverter);
		restTemplate.setInterceptors(List.of(loggingRequestInterceptor));
		return restTemplate;
	}


}
