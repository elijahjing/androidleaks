package com.leak.sdk.leaklibrary;


import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class LeakWeakReference  extends WeakReference<Object> {
    public final String key;
    public final String name;
    LeakWeakReference(Object referent, String key, String name,
                      ReferenceQueue<Object> referenceQueue) {
        super( Utils.checkNotNull(referent, "referent"),  Utils.checkNotNull(referenceQueue, "referenceQueue"));
        this.key = Utils.checkNotNull(key, "key");
        this.name = Utils.checkNotNull(name, "name");
    }
}