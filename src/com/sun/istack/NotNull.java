package com.sun.istack;

 import java.lang.annotation.Documented;

 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;

 import java.lang.annotation.Target;
 import java.lang.annotation.ElementType;
@Documented
@Retention(RetentionPolicy.CLASS)

@Target({ElementType.FIELD,ElementType.METHOD,ElementType.PARAMETER,ElementType.LOCAL_VARIABLE})

public @interface NotNull {

}