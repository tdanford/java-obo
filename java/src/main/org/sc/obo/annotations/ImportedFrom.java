package org.sc.obo.annotations;

import java.lang.annotation.Retention;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface ImportedFrom {
	public String value();
}
