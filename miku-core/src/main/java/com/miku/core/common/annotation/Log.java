package com.miku.core.common.annotation;

import lombok.Getter;

import java.lang.annotation.*;

/**
 * 操作日志记录注解
 *
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    String title() default "";

    BusinessType businessType() default BusinessType.OTHER;

    OperatorType operatorType() default OperatorType.MANAGE;

    boolean isSaveRequestData() default true;

    boolean isSaveResponseData() default true;

    @Getter
    enum BusinessType {
        OTHER(0),
        INSERT(1),
        UPDATE(2),
        DELETE(3),
        GRANT(4),
        EXPORT(5),
        IMPORT(6),
        FORCE(7),
        CLEAN(8);

        private final int value;

        BusinessType(int value) {
            this.value = value;
        }
    }

    @Getter
    enum OperatorType {
        OTHER(0),
        MANAGE(1),
        CLIENT(2);

        private final int value;

        OperatorType(int value) {
            this.value = value;
        }
    }
}


