package com.project.souklab.controller.support;

import com.project.souklab.filestorage.security.FileRateLimitFilter;
import com.project.souklab.security.JwtAuthenticationFilter;
import com.project.souklab.security.RateLimitFilter;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite test annotation for controller slice testing.
 * Combines Boot 4 @WebMvcTest, excludes filter components (JwtAuthenticationFilter,
 * RateLimitFilter, FileRateLimitFilter) that require unmocked services, and imports ControllerSliceSecurityConfig.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@WebMvcTest(
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, RateLimitFilter.class, FileRateLimitFilter.class}
        )
)
@Import(ControllerSliceSecurityConfig.class)
public @interface ControllerSliceTest {

    /**
     * Target controller class(es) to load into the slice context.
     */
    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] controllers() default {};

    /**
     * Alias for controllers().
     */
    @AliasFor(annotation = WebMvcTest.class, attribute = "value")
    Class<?>[] value() default {};
}
