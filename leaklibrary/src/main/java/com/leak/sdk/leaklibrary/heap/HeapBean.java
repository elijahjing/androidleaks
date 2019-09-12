package com.leak.sdk.leaklibrary.heap;

import java.io.File;
import java.io.Serializable;

public class HeapBean implements Serializable {
    File file;
    public HeapBean(File file){
        this.file=file;

    }
}
