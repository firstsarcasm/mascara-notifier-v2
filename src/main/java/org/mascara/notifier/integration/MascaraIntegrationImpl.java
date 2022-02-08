package org.mascara.notifier.integration;

import lombok.RequiredArgsConstructor;
import org.mascara.notifier.mapping.AvailableBookingTimeMapper;
import org.mascara.notifier.model.TimePeriod;
import org.mascara.notifier.model.days.response.BookDatesResponse;
import org.mascara.notifier.model.staff.response.Employee;
import org.mascara.notifier.model.times.response.FreeBookingTime;
import org.mascara.notifier.util.TimeUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mascara.notifier.constant.Studio.UZHNAYA;
import static org.mascara.notifier.constant.WorkTime.END_OF_WORK;
import static org.mascara.notifier.constant.WorkTime.START_OF_WORK;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.ObjectUtils.isEmpty;

@Component
@RequiredArgsConstructor
public class MascaraIntegrationImpl implements MascaraIntegration {

	private static final String STAFF_URL_TEMPLATE = "https://n652960.yclients.com/api/v1/book_staff/%s";
	private static final String TIMETABLE_URL_TEMPLATE = "https://n652960.yclients.com/api/v1/book_staff_seances/%s/%s";
	private static final String BOOK_DATES_URL_TEMPLATE = "https://n652960.yclients.com/api/v1/book_dates/%s?staff_id=%s&date=%s";
	private static final String BOOK_TIMES_URL_TEMPLATE = "https://n652960.yclients.com/api/v1/book_times/%s/%s/%s";

	//todo move to config
	private static final String MASCARA_TOKEN = "Bearer yusw3yeu6hrr4r9j3gw6";

	private final RestTemplate customRestTemplate;
	private final AvailableBookingTimeMapper availableBookingTimeMapper;

	@Override
	public List<TimePeriod> getBookedTime(Integer staffId, LocalDate date) {
		URI uri = URI.create(String.format(BOOK_TIMES_URL_TEMPLATE, UZHNAYA.getCode(), staffId, date));
		RequestEntity<Void> request = makeGetRequest(uri);

		var response = customRestTemplate.exchange(request, FreeBookingTime[].class);
		List<TimePeriod> freeTimePeriods = toTimePeriods(response);

		return getBookedTimeListFromFreeTime(freeTimePeriods);
	}

	@Override
	public List<String> getBookDates(Integer staffId) {
		URI uri = URI.create(String.format(BOOK_DATES_URL_TEMPLATE, UZHNAYA.getCode(), staffId, TimeUtils.getToday()));
		RequestEntity<Void> request = makeGetRequest(uri);

		var response = customRestTemplate.exchange(request, BookDatesResponse.class);
		return Optional.of(response)
				.map(HttpEntity::getBody)
				.map(BookDatesResponse::getWorkingDates)
				.orElse(null);
	}

	private RequestEntity<Void> makeGetRequest(URI uri) {
		return RequestEntity.get(uri)
				.accept(MediaType.APPLICATION_JSON)
				.header(AUTHORIZATION, MASCARA_TOKEN)
				.build();
	}

	@Override
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

	private Integer getIdOrThrow(String name, Stream<Employee> staffResponse) {
		return staffResponse.filter(employee -> name.equals(employee.getName()))
				.findFirst()
				.map(Employee::getId)
				.orElseThrow();
	}

	private List<TimePeriod> getBookedTimeListFromFreeTime(List<TimePeriod> possibleServiceTimePeriods) {
		List<TimePeriod> bookedPeriods = new ArrayList<>();
		if (isEmpty(possibleServiceTimePeriods)) {
			return bookedPeriods;
		}

		LocalTime starTime = possibleServiceTimePeriods.get(0).getStarTime();
		if (!starTime.equals(START_OF_WORK)) {
			bookedPeriods.add(new TimePeriod(START_OF_WORK, starTime));
		}

		fillInWithRecords(possibleServiceTimePeriods, bookedPeriods);

		return bookedPeriods;
	}

	private void fillInWithRecords(List<TimePeriod> possibleServiceTimePeriods, List<TimePeriod> result) {
		for (int i = 0; i < possibleServiceTimePeriods.size(); i++) {
			var startOfOrder = possibleServiceTimePeriods.get(i).getEndTime();
			if (startOfOrder == END_OF_WORK) {
				break;
			}
			if (i + 1 > possibleServiceTimePeriods.size() - 1) {
				result.add(new TimePeriod(startOfOrder, END_OF_WORK));
				break;
			}

			var endOfOrder = possibleServiceTimePeriods.get(i + 1).getStarTime();
			if (startOfOrder.isBefore(endOfOrder)) {
				result.add(new TimePeriod(startOfOrder, endOfOrder));
			}
		}
	}

	private List<TimePeriod> toTimePeriods(ResponseEntity<FreeBookingTime[]> response) {
		return Optional.ofNullable(response)
				.map(HttpEntity::getBody).stream()
				.flatMap(Arrays::stream)
				.map(availableBookingTimeMapper::toTimePeriod)
				.toList();
	}
}
