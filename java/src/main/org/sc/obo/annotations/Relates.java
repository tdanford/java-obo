package org.sc.obo.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface Relates {
	public String value() default "";
}
