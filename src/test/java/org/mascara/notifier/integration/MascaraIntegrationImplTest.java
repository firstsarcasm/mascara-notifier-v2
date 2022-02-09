package org.mascara.notifier.integration;

import org.junit.jupiter.api.Test;
import org.mascara.notifier.mapping.AvailableBookingTimeMapper;
import org.mascara.notifier.model.TimePeriod;
import org.mascara.notifier.model.times.response.FreeBookingTime;
import org.mascara.notifier.util.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = {
		MascaraIntegrationImpl.class,
		AvailableBookingTimeMapper.class
})
class MascaraIntegrationImplTest {

	@Autowired
	private MascaraIntegration integration;

	@MockBean
	private RestTemplate customRestTemplate;

	@Test
	void getBookedTime() {

		List<FreeBookingTime> build = List.of(
				FreeBookingTime.builder().time(LocalTime.of(10, 15)).seanceLength(7200L).build(),
				FreeBookingTime.builder().time(LocalTime.of(10, 30)).seanceLength(5400L).build()
		);
		FreeBookingTime[] objects1 = build.toArray(new FreeBookingTime[build.size()]);
		when(customRestTemplate.exchange(any(RequestEntity.class), eq(FreeBookingTime[].class)))
				.thenReturn(ResponseEntity.ok(objects1));


		LocalDate now = TimeUtils.getToday();
		List<TimePeriod> bookedTime = integration.getBookedTime(123, now);

		System.out.println(bookedTime);

	}
}