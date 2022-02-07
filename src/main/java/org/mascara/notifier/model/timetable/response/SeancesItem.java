package org.mascara.notifier.model.timetable.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeancesItem {
	private Integer seanceLength;
	private String datetime;
	private Integer sumLength;
	private String time;
}
