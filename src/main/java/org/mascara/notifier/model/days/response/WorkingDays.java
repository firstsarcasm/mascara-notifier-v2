package org.mascara.notifier.model.days.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkingDays{
	private List<Integer> jsonMember2;
}