package org.springframework.core.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Sam Brannen
 * @author wangheng
 * @date 2019/09/29
 * @since 4.2
 * @see MergedAnnotations
 * @see SynthesizedAnnotation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface AliasFor {

	@AliasFor("attribute")
	String value() default "";

	@AliasFor("value")
	String attribute() default "";

	Class<? extends Annotation> annotation() default Annotation.class;

}
