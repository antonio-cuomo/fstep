package com.cgi.eoss.fstep.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UsageType {

	MAX_RUNNABLE_JOBS(5L), 
	FILES_STORAGE_MB(5L),
	PERSISTENT_STORAGE_MB(5L);
	
	private final Long defaultValue;
}
