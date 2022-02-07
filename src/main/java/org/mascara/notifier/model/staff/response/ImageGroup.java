package org.mascara.notifier.model.staff.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageGroup{
	private Images images;
	private Integer id;
	private String entityId;
	private String entity;
}
