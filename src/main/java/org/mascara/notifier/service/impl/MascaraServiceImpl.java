package org.mascara.notifier.service.impl;

import lombok.RequiredArgsConstructor;
import org.mascara.notifier.integration.MascaraIntegrationImpl;
import org.mascara.notifier.model.TimePeriod;
import org.mascara.notifier.service.MascaraService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.mascara.notifier.constant.WorkTime.END_OF_WORK;
import static org.mascara.notifier.constant.WorkTime.START_OF_WORK;

//todo constants
@Service
@RequiredArgsConstructor
public class MascaraServiceImpl implements MascaraService {

	private final MascaraIntegrationImpl integration;

	@Override
	public String getSchedule(Integer staffId, LocalDate day) {
		List<TimePeriod> bookedTime = integration.getBookedTime(staffId, day);

		if (bookedTime.isEmpty()) {
			return "выходной!";
		}

		TimePeriod firstBookedTime = bookedTime.get(0);
		if (isStartOfWork(firstBookedTime) && isEndOfWork(firstBookedTime)) {
			return "записей нет(";
		}

		return bookedTime.stream()
				.map(period -> "Запись: %s - %s\n".formatted(period.getStarTime(), period.getEndTime()))
				.collect(Collectors.joining());
	}

	@Override
	public String getScheduleFormatted(Integer staffId, String prefix, LocalDate day) {
		String schedule = getSchedule(staffId, day);
		return "%s \n%s".formatted(prefix, schedule);
	}

	@Override
	public String getDatesFormatted(Integer staffId) {
		return String.join(System.lineSeparator(), integration.getBookDates(staffId));
	}

	@Override
	public Integer getStaffId(String employeeName) {
		return integration.getEmployeeIdByName(employeeName);
	}


	private boolean isStartOfWork(TimePeriod first) {
		return START_OF_WORK.equals(first.getStarTime());
	}

	private boolean isEndOfWork(TimePeriod first) {
		return END_OF_WORK.equals(first.getEndTime());
	}

}

