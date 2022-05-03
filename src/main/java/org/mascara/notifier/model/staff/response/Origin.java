package org.mascara.notifier.model.staff.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Origin {
	private String path;
	private String width;
	private Integer id;
	private String type;
	private Integer imageGroupId;
	private String version;
	private String height;
}
