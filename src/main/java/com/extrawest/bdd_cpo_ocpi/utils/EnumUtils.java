package com.extrawest.bdd_cpo_ocpi.utils;

import java.util.function.Function;

public final class EnumUtils {
    public static <T extends Enum<T>> T findByField(Class<T> enumType,
                                                    Function<T, String> fieldSelector,
                                                    String fieldValue) {
        for (T enumValue : enumType.getEnumConstants()) {
            if (fieldSelector.apply(enumValue).equals(fieldValue)) {
                return enumValue;
            }
        }
        throw new IllegalArgumentException("No enum constant %s with field value %s"
                .formatted(enumType.getName(), fieldValue));
    }
}
