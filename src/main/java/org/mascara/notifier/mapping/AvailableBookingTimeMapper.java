package org.mascara.notifier.mapping;

import org.mascara.notifier.logging.LogEntryAndExit;
import org.mascara.notifier.model.TimePeriod;
import org.mascara.notifier.model.times.response.FreeBookingTime;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class AvailableBookingTimeMapper {

	@LogEntryAndExit
	public TimePeriod toTimePeriod(FreeBookingTime possibleRecord) {
		LocalTime startTime = possibleRecord.getTime();
		LocalTime endTime = startTime.plusSeconds(possibleRecord.getSeanceLength());
		return new TimePeriod(startTime, endTime);
	}

}
