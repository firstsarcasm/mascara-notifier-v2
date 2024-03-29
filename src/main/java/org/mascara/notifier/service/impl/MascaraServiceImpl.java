package org.mascara.notifier.service.impl;

import lombok.RequiredArgsConstructor;
import org.mascara.notifier.constant.RelativeDay;
import org.mascara.notifier.integration.MascaraIntegrationImpl;
import org.mascara.notifier.logging.LogEntryAndExit;
import org.mascara.notifier.model.TimePeriod;
import org.mascara.notifier.service.MascaraService;
import org.mascara.notifier.util.TimeUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MascaraServiceImpl implements MascaraService {

	private static final String DAY_DESCRIPTION_DELIMITER = ":";

	private final MascaraIntegrationImpl integration;

	@Override
	@LogEntryAndExit
	public String getSchedule(Integer staffId, LocalDate day) {
		List<TimePeriod> bookedTime = integration.getBookedTime(staffId, day);

		if (bookedTime.isEmpty()) {
			if (isWorkingDay(staffId, day)) {
				return "записей нет(";
			}
			return "выходной!";
		}

		return bookedTime.stream()
				.map(period -> "Запись: %s - %s\n".formatted(period.getStarTime(), period.getEndTime()))
				.collect(Collectors.joining());
	}

	private boolean isWorkingDay(Integer staffId, LocalDate day) {
		List<LocalDate> workingDates = integration.getBookingDates(staffId, TimeUtils.getToday().withDayOfMonth(1));
		return workingDates.contains(day);
	}

	@Override
	@LogEntryAndExit
	public String getScheduleFormatted(Integer staffId, String prefix, LocalDate day) {
		String schedule = getSchedule(staffId, day);
		return "%s \n%s".formatted(prefix, schedule);
	}

	@Override
	@LogEntryAndExit
	public String getDatesFormatted(Integer staffId) {
		return integration.getBookingDates(staffId).stream()
				.map(LocalDate::toString)
				.collect(Collectors.joining(System.lineSeparator()));
	}

	@Override
	@LogEntryAndExit
	public Integer getStaffId(String employeeName) {
		return integration.getEmployeeIdByName(employeeName);
	}

	@Override
	@LogEntryAndExit
	public LinkedHashMap<RelativeDay, String> getScheduleForTargetDays(Integer staffId) {
		return Arrays.stream(RelativeDay.values()).collect(Collectors.toMap(
				Function.identity(),
				relativeDay -> getScheduleForTargetDay(staffId, relativeDay),
				(o1, o2) -> o1, LinkedHashMap::new
		));
	}

	private String getScheduleForTargetDay(Integer staffId, RelativeDay relativeDay) {
		String prefix = makePrefix(relativeDay);
		LocalDate targetDay = relativeDay.getDay().get();
		return getScheduleFormatted(staffId, prefix, targetDay);
	}

	private String makePrefix(RelativeDay relativeDay) {
		return relativeDay.getDescription() + DAY_DESCRIPTION_DELIMITER;
	}


}

