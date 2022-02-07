package org.mascara.notifier.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimePeriod {
	private LocalTime starTime;
	private LocalTime endTime;
}
