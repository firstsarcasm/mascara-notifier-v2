package org.mascara.notifier.integration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mascara.notifier.entity.BookedTimeCache;
import org.mascara.notifier.mapping.AvailableBookingTimeMapper;
import org.mascara.notifier.mapping.BookedTimeMapper;
import org.mascara.notifier.model.TimePeriod;
import org.mascara.notifier.model.times.response.FreeBookingTime;
import org.mascara.notifier.repository.BookedTimeCacheRepository;
import org.mascara.notifier.util.TimeUtils;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

//todo refactoring pls
@SpringJUnitConfig(classes = {
		MascaraIntegrationImpl.class,
		AvailableBookingTimeMapper.class,
		BookedTimeMapper.class
})
class MascaraIntegrationImplTest {

	@Autowired
	private MascaraIntegration integration;

	@MockBean
	private RestTemplate customRestTemplate;

	@MockBean
	private BookedTimeCacheRepository bookedTimeCacheRepository;

	@Test
	@SneakyThrows
	@DisplayName("Should return previous time when current time crossed the border of current session")
	void test1() {
		TimePeriod previousTimePeriod = new TimePeriod(LocalTime.of(16, 0), LocalTime.of(18, 0));
		when(bookedTimeCacheRepository.findByStaffIdAndDate(anyInt(), any(LocalDate.class))).thenReturn(BookedTimeCache.builder()
						.schedule(List.of(
								previousTimePeriod
						))
				.build());
		try (MockedStatic<TimeUtils> utilities = Mockito.mockStatic(TimeUtils.class)) {
			LocalDateTime actualDateTime = LocalDateTime.parse("2022-12-30T16:10:00.11");
			utilities.when(TimeUtils::getTodayDateTime).thenReturn(actualDateTime);
			List<FreeBookingTime> build = List.of(
					FreeBookingTime.builder().time(LocalTime.of(18, 0)).seanceLength(14400L).build()
			);
			FreeBookingTime[] objects1 = build.toArray(new FreeBookingTime[build.size()]);
			when(customRestTemplate.exchange(any(RequestEntity.class), eq(FreeBookingTime[].class)))
					.thenReturn(ResponseEntity.ok(objects1));


			LocalDate now = LocalDate.parse("2022-12-30");
			List<TimePeriod> bookedTime = integration.getBookedTime(123, now);

			assertEquals(List.of(previousTimePeriod), bookedTime);
		}

	}

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