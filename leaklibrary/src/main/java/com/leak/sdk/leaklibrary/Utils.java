package com.leak.sdk.leaklibrary;


import androidx.annotation.NonNull;

public final class Utils {
    //高版本兼容
    @NonNull
    public static <T> T checkNotNull(T reference, String errorMessage) {
        if (reference == null) {
            throw new NullPointerException(errorMessage +"is  null");
        } else {
            return reference;
        }
    }
}
