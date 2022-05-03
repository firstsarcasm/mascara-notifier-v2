package org.mascara.notifier.model.days.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDatesResponse {
	@JsonProperty("working_dates")
	private List<LocalDate> bookingDates;

	@JsonProperty("booking_dates")
	private List<LocalDate> workingDates;
}