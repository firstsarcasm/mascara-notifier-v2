package org.mascara.notifier.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.mascara.notifier.model.TimePeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.List;

@Slf4j
@Converter
@Component
public class BookedTimeConverter implements AttributeConverter<List<TimePeriod>, String> {

	private static final TypeReference<List<TimePeriod>> TYPE_REF = new TypeReference<>() {};

	private static ObjectMapper objectMapper;

	@Autowired
	public void setObjectMapper(ObjectMapper objectMapper) {
		BookedTimeConverter.objectMapper = objectMapper;
	}

	@Override
	public String convertToDatabaseColumn(List<TimePeriod> object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (final JsonProcessingException e) {
			log.error("JSON writing error", e);
			return null;
		}
	}

	@Override
	public List<TimePeriod> convertToEntityAttribute(String str) {
		try {
			return objectMapper.readValue(str, TYPE_REF);
		} catch (final IOException e) {
			log.error("JSON reading error", e);
			return null;
		}
	}

}