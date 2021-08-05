package org.lin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Lin =￣ω￣=
 * @date 2021/6/24
 */
@Retention(RUNTIME)
@Target({ElementType.TYPE})
public @interface Parser {

	String name();

	String type() default "parser";

	int weight() default 0;

	String note() default "";

}
