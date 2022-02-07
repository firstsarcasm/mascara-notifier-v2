package org.mascara.notifier.model.timetable.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimetableResponse {
	private List<SeancesItem> seances;
	private String seanceDate;
}