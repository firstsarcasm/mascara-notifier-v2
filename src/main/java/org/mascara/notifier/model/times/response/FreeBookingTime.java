package org.mascara.notifier.model.times.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FreeBookingTime {
	@JsonProperty("seance_length")
	private Long seanceLength;

	private ZonedDateTime datetime;

	@JsonProperty("sum_length")
	private Long sumLength;

	private LocalTime time;
}
