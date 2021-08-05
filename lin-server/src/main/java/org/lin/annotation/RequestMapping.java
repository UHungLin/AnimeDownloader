package org.lin.annotation;

import java.lang.annotation.*;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/21
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {

	String[] value() default {};

}
