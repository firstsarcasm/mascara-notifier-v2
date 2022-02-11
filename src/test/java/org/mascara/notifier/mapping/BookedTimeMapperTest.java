package org.mascara.notifier.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mascara.notifier.model.TimePeriod;
import org.mascara.notifier.model.times.response.FreeBookingTime;
import org.mascara.notifier.utils.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

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

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@SneakyThrows
	@DisplayName("shouldn't throw an exception")
	void test1() {
		String responseBody = ResourceUtils.resourceToString("integration/response/book.times/bookTimesResponse_no_records.json");
		var responseObject = objectMapper.readValue(responseBody, FreeBookingTime[].class);
		List<TimePeriod> timePeriods = toTimePeriods(ResponseEntity.ok(responseObject));
		List<TimePeriod> bookedTime = bookedTimeMapper.fromFreeTime(timePeriods, now());
		System.out.println(bookedTime);
	}

	private List<TimePeriod> toTimePeriods(ResponseEntity<FreeBookingTime[]> response) {
		return Optional.ofNullable(response)
				.map(HttpEntity::getBody).stream()
				.flatMap(Arrays::stream)
				.map(availableBookingTimeMapper::toTimePeriod)
				.toList();
	}

}