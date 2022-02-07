package org.mascara.notifier.integration;

import org.mascara.notifier.model.TimePeriod;

import java.time.LocalDate;
import java.util.List;

public interface MascaraIntegration {
	List<TimePeriod> getBookedTime(Integer staffId, LocalDate date);

	List<String> getBookDates(Integer staffId);

	Integer getEmployeeIdByName(String name);
}
