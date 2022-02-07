package org.mascara.notifier.model.staff.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
	private Integer fired;
	private Object apiId;
	private Boolean bookable;
	private Integer hidden;
	private String avatarBig;
	private Integer rating;
	private Integer weight;
	private String prepaid;
	private String avatar;
	private Integer showRating;
	private Integer votesCount;
	private Integer commentsCount;
	private ImageGroup imageGroup;
	private String name;
	private String specialization;
	private String information;
	private Integer id;
	private Position position;
	private String scheduleTill;
	private Object user;
	private Integer positionId;
	private Integer status;
}