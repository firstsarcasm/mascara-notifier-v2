package org.mascara.notifier.model.days.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDatesResponse {
	@JsonProperty("working_dates")
	private List<String> bookingDates;
	//	private WorkingDays workingDays;
//	private BookingDays bookingDays;
	@JsonProperty("booking_dates")
	private List<String> workingDates;
}