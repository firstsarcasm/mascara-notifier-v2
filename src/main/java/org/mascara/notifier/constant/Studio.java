package org.mascara.notifier.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Studio {
	UZHNAYA("Южная", 617387)
	;

	private final String name;
	private final Integer code;
}