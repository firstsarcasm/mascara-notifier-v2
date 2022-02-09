package org.mascara.notifier.integration;

import org.mascara.notifier.model.TimePeriod;

import java.time.LocalDate;
import java.util.List;

public interface MascaraIntegration {
	List<TimePeriod> getBookedTime(Integer staffId, LocalDate date);

	List<LocalDate> getBookingDates(Integer staffId);

	List<LocalDate> getBookingDates(Integer staffId, LocalDate startOfThePeriod);

	Integer getEmployeeIdByName(String name);
}
