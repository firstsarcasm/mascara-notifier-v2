package org.mascara.notifier.service;

import org.mascara.notifier.constant.RelativeDay;

import java.time.LocalDate;
import java.util.LinkedHashMap;

public interface MascaraService {

	String getSchedule(Integer staffId, LocalDate day);

	String getScheduleFormatted(Integer staffId, String prefix, LocalDate day);

	String getDatesFormatted(Integer staffId);

	Integer getStaffId(String employeeName);

	LinkedHashMap<RelativeDay, String> getScheduleForTargetDays(Integer staffId);
}
