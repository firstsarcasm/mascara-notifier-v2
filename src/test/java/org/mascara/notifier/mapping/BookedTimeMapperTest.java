package org.mascara.notifier.mapping;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mascara.notifier.model.TimePeriod;
import org.mascara.notifier.model.times.response.FreeBookingTime;
import org.mascara.notifier.util.TimeUtils;
import org.mascara.notifier.utils.ResourceUtils;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDate.now;

@SpringJUnitConfig(classes = {
		BookedTimeMapper.class,
		AvailableBookingTimeMapper.class,
		ObjectMapper.class
})
class BookedTimeMapperTest {

	@Autowired
	private BookedTimeMapper bookedTimeMapper;

	@Autowired
	private AvailableBookingTimeMapper availableBookingTimeMapper;

	private final ObjectMapper customObjectMapper = customObjectMapper();

	@Test
	@SneakyThrows
	@DisplayName("shouldn't throw an exception")
	void test1() {
		String responseBody = ResourceUtils.resourceToString("integration/response/book.times/bookTimesResponse_1_record_in_the_beggining_of_the_day.json");
		var responseObject = customObjectMapper.readValue(responseBody, FreeBookingTime[].class);
		List<TimePeriod> timePeriods = toTimePeriods(ResponseEntity.ok(responseObject));

		try (MockedStatic<TimeUtils> utilities = Mockito.mockStatic(TimeUtils.class)) {
			LocalDateTime date = LocalDateTime.of(2022, 12, 2, 11, 0);
			utilities.when(TimeUtils::getTodayDateTime).thenReturn(date);
			List<TimePeriod> bookedTime = bookedTimeMapper.fromFreeTime(timePeriods, LocalDate.of(2022, 12, 2));
			System.out.println(bookedTime);
		}
	}

	private List<TimePeriod> toTimePeriods(ResponseEntity<FreeBookingTime[]> response) {
		return Optional.ofNullable(response)
				.map(HttpEntity::getBody).stream()
				.flatMap(Arrays::stream)
				.map(availableBookingTimeMapper::toTimePeriod)
				.toList();
	}

	private ObjectMapper customObjectMapper() {
		return new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
				.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

}