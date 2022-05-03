package org.mascara.notifier.integration;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Slf4j
@SpringJUnitConfig(classes = {
		MascaraIntegrationImpl.class,
		AvailableBookingTimeMapper.class,
		BookedTimeMapper.class
})
class MascaraIntegrationImplTest {

	private static final LocalTime EIGHTEEN = LocalTime.of(18, 0);
	private static final LocalTime SIXTEEN = LocalTime.of(16, 0);
	private static final LocalTime FIFTEEN_AFTER_TEN = LocalTime.of(10, 15);
	private static final LocalTime THIRTY_AFTER_TEN = LocalTime.of(10, 30);
	private static final LocalDateTime ACTUAL_DATE_TIME_FOR_TEST = LocalDateTime.parse("2022-12-30T15:45:00.11");

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
		TimePeriod previousTimePeriod = new TimePeriod(SIXTEEN, EIGHTEEN);
		BookedTimeCache bookedTimeCache = BookedTimeCache.builder()
				.schedule(List.of(previousTimePeriod))
				.build();
		when(bookedTimeCacheRepository.findByStaffIdAndDate(anyInt(), any(LocalDate.class))).thenReturn(bookedTimeCache);
		try (MockedStatic<TimeUtils> utilities = Mockito.mockStatic(TimeUtils.class)) {
			utilities.when(TimeUtils::getTodayDateTime).thenReturn(ACTUAL_DATE_TIME_FOR_TEST);
			List<FreeBookingTime> bookingTimes = Collections.singletonList(makeFreeBookingTimeArray(EIGHTEEN, 14400L));
			FreeBookingTime[] objects1 = makeFreeBookingTimeArray(bookingTimes);
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
				makeFreeBookingTimeArray(FIFTEEN_AFTER_TEN, 7200L),
				makeFreeBookingTimeArray(THIRTY_AFTER_TEN, 5400L)
		);
		FreeBookingTime[] objects1 = build.toArray(new FreeBookingTime[build.size()]);
		when(customRestTemplate.exchange(any(RequestEntity.class), eq(FreeBookingTime[].class)))
				.thenReturn(ResponseEntity.ok(objects1));

		LocalDate now = TimeUtils.getToday();
		List<TimePeriod> bookedTime = integration.getBookedTime(123, now);

		log.info(bookedTime.toString());
	}

	private FreeBookingTime makeFreeBookingTimeArray(LocalTime time, long seanceLength) {
		return FreeBookingTime.builder()
				.time(time)
				.seanceLength(seanceLength)
				.build();
	}

	private FreeBookingTime[] makeFreeBookingTimeArray(List<FreeBookingTime> bookingTimes) {
		return bookingTimes.toArray(new FreeBookingTime[0]);
	}
}