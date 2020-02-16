package xyz.rexlin600.aop.aspect.two.annotation;

import java.lang.annotation.*;

/**
 * SysLog interface
 *
 * @author: rexlin600
 * @date: 2020-02-16
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SysAnnotationLog {

    /**
     * 描述
     *
     * @return {String}
     */
    String value();

}