package org.mascara.notifier.integration;

import lombok.RequiredArgsConstructor;
import org.mascara.notifier.entity.BookedTimeCache;
import org.mascara.notifier.logging.LogEntryAndExit;
import org.mascara.notifier.mapping.AvailableBookingTimeMapper;
import org.mascara.notifier.mapping.BookedTimeMapper;
import org.mascara.notifier.model.TimePeriod;
import org.mascara.notifier.model.days.response.BookDatesResponse;
import org.mascara.notifier.model.staff.response.Employee;
import org.mascara.notifier.model.times.response.FreeBookingTime;
import org.mascara.notifier.repository.BookedTimeCacheRepository;
import org.mascara.notifier.util.TimeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.mascara.notifier.constant.Studio.UZHNAYA;
import static org.mascara.notifier.constant.WorkTime.START_OF_WORK;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.ObjectUtils.isEmpty;

@Component
@RequiredArgsConstructor
public class MascaraIntegrationImpl implements MascaraIntegration {

	private static final String STAFF_URL_TEMPLATE = "https://n652960.yclients.com/api/v1/book_staff/%s";
	private static final String BOOK_DATES_URL_TEMPLATE = "https://n652960.yclients.com/api/v1/book_dates/%s?staff_id=%s&date=%s";
	private static final String BOOK_TIMES_URL_TEMPLATE = "https://n652960.yclients.com/api/v1/book_times/%s/%s/%s";

	private final RestTemplate customRestTemplate;
	private final AvailableBookingTimeMapper availableBookingTimeMapper;
	private final BookedTimeMapper bookedTimeMapper;
	private final BookedTimeCacheRepository bookedTimeCacheRepository;

	@Value("${mascara.token}")
	private String mascaraToken;

	@Override
	@LogEntryAndExit
	public List<TimePeriod> getBookedTime(Integer staffId, LocalDate date) {
		URI uri = URI.create(String.format(BOOK_TIMES_URL_TEMPLATE, UZHNAYA.getCode(), staffId, date));
		RequestEntity<Void> request = makeGetRequest(uri);

		var response = customRestTemplate.exchange(request, FreeBookingTime[].class);
		List<TimePeriod> freeTimePeriods = toTimePeriods(response);

		List<TimePeriod> actualBookedTime = bookedTimeMapper.fromFreeTime(freeTimePeriods, date);
		return returnFromCacheIfChangesInsignificant(actualBookedTime, staffId, date);
	}

	private List<TimePeriod> returnFromCacheIfChangesInsignificant(List<TimePeriod> actualBookedTime, Integer staffId, LocalDate date) {
		LocalDateTime actualDateTime = TimeUtils.getTodayDateTime();
		if (!actualDateTime.toLocalDate().isEqual(date)) {
			return actualBookedTime;
		}
		BookedTimeCache bookedTimeCache = bookedTimeCacheRepository.findByStaffIdAndDate(staffId, date);
		if (isNull(bookedTimeCache)) {
			saveToDb(date, staffId, actualBookedTime, bookedTimeCache);
			return actualBookedTime;
		}
		if (!isEmpty(actualBookedTime) && actualBookedTime.size() == 1 && actualBookedTime.get(0).getStarTime().plusMinutes(5).equals(LocalTime.of(22, 0))) {
			return emptyList();
		}

		List<TimePeriod> previousBookedTime = bookedTimeCache.getSchedule();
		if (!isEmpty(actualBookedTime) && !actualBookedTime.equals(previousBookedTime) && actualBookedTime.size() == previousBookedTime.size()) {
			if (actualBookedTime.get(0).getStarTime().equals(START_OF_WORK) && (actualDateTime.toLocalTime().isAfter(previousBookedTime.get(0).getStarTime()) || actualDateTime.toLocalTime().isAfter(previousBookedTime.get(0).getStarTime().minusMinutes(16)))) {
				return previousBookedTime;
			}

			List<TimePeriod> stabilizedBookedTime = new LinkedList<>();
			for (int i = 0; i < actualBookedTime.size(); i++) {
				TimePeriod actualItem = actualBookedTime.get(i);
				TimePeriod previousItem = previousBookedTime.get(i);
				if (actualItem.getStarTime().equals(previousItem.getStarTime())) {
					stabilizedBookedTime.add(actualItem);
					continue;
				}
				if (actualItem.getStarTime().plusMinutes(5).equals(previousItem.getStarTime())
						|| actualItem.getStarTime().minusMinutes(5).equals(previousItem.getStarTime())) {
					stabilizedBookedTime.add(previousItem);
				} else {
					saveToDb(date, staffId, actualBookedTime, bookedTimeCache);
					return actualBookedTime;
				}
			}
			return stabilizedBookedTime;
		}
		saveToDb(date, staffId, actualBookedTime, bookedTimeCache);
		return actualBookedTime;
	}

	private void saveToDb(LocalDate date, Integer staffId, List<TimePeriod> actualBookedTime, BookedTimeCache bookedTimeCache) {
		if (isNull(bookedTimeCache)) {
			bookedTimeCacheRepository.save(BookedTimeCache.builder()
					.date(date)
					.staffId(staffId)
					.schedule(actualBookedTime)
					.build());
			return;
		}
		bookedTimeCache.setSchedule(actualBookedTime);
		bookedTimeCacheRepository.save(bookedTimeCache);
	}

	@Override
	@LogEntryAndExit
	public List<LocalDate> getBookingDates(Integer staffId) {
		return getBookingDates(staffId, TimeUtils.getToday());
	}

	@Override
	@LogEntryAndExit
	@Cacheable("bookedDates")
	public List<LocalDate> getBookingDates(Integer staffId, LocalDate startOfThePeriod) {
		URI uri = URI.create(String.format(BOOK_DATES_URL_TEMPLATE, UZHNAYA.getCode(), staffId, startOfThePeriod));
		RequestEntity<Void> request = makeGetRequest(uri);

		var response = customRestTemplate.exchange(request, BookDatesResponse.class);
		return Optional.of(response)
				.map(HttpEntity::getBody)
				.map(BookDatesResponse::getBookingDates)
				.orElse(null);
	}

	@Override
	@LogEntryAndExit
	public Integer getEmployeeIdByName(String name) {
		URI uri = URI.create(String.format(STAFF_URL_TEMPLATE, UZHNAYA.getCode()));
		RequestEntity<Void> request = makeGetRequest(uri);

		var response = customRestTemplate.exchange(request, Employee[].class);
		return Optional.of(response)
				.map(HttpEntity::getBody)
				.map(Arrays::stream)
				.map(ss -> getIdOrThrow(name, ss))
				.orElse(null);
	}

	private RequestEntity<Void> makeGetRequest(URI uri) {
		return RequestEntity.get(uri)
				.accept(MediaType.APPLICATION_JSON)
				.header(AUTHORIZATION, mascaraToken)
				.build();
	}

	private Integer getIdOrThrow(String name, Stream<Employee> staffResponse) {
		return staffResponse.filter(employee -> name.equals(employee.getName()))
				.findFirst()
				.map(Employee::getId)
				.orElseThrow();
	}

	private List<TimePeriod> toTimePeriods(ResponseEntity<FreeBookingTime[]> response) {
		return Optional.ofNullable(response)
				.map(HttpEntity::getBody).stream()
				.flatMap(Arrays::stream)
				.map(availableBookingTimeMapper::toTimePeriod)
				.toList();
	}
}
