package org.mascara.notifier.service;

import java.time.LocalDate;

public interface MascaraService {

	String getSchedule(Integer staffId, LocalDate day);

	String getScheduleFormatted(Integer staffId, String prefix, LocalDate day);

	String getDatesFormatted(Integer staffId);

	Integer getStaffId(String employeeName);

}
