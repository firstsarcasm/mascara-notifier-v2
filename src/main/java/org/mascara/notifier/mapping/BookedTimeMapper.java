package org.mascara.notifier.mapping;

import org.mascara.notifier.model.TimePeriod;
import org.mascara.notifier.util.TimeUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.mascara.notifier.constant.WorkTime.END_OF_WORK;
import static org.mascara.notifier.constant.WorkTime.START_OF_WORK;
import static org.springframework.util.ObjectUtils.isEmpty;

@Component
public class BookedTimeMapper {

	public List<TimePeriod> fromFreeTime(List<TimePeriod> possibleServiceTimePeriods, LocalDate date) {
		List<TimePeriod> bookedPeriods = new ArrayList<>();
		if (isEmpty(possibleServiceTimePeriods)) {
			return bookedPeriods;
		}

		//todo we need some refactoring here
		LocalTime starTime = possibleServiceTimePeriods.get(0).getStarTime();
		LocalDateTime today = TimeUtils.getTodayDateTime();
		LocalTime localTime = today.toLocalTime().plusMinutes(15);
		if (!starTime.equals(START_OF_WORK) && !(date.isEqual(today.toLocalDate()) && (localTime.isAfter(starTime) || localTime.equals(starTime)))) {
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
}
