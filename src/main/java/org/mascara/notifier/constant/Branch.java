package org.mascara.notifier.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Branch {
	UZHNAYA("Южная", 617387)
	;

	private final String branchName;
	private final Integer code;
}