package org.mascara.notifier.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.mascara.notifier.config.BookedTimeConverter;
import org.mascara.notifier.model.TimePeriod;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(schema = "mascara", name = "booked_time_cache")
public class BookedTimeCache {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	private Integer staffId;

	private LocalDate date;

	@Convert(converter = BookedTimeConverter.class)
	private List<TimePeriod> schedule;

}





