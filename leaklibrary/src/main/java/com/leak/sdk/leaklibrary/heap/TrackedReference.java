package com.leak.sdk.leaklibrary.heap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * heap was dumped. May or may not point to a leaking reference.
 */
public class TrackedReference {

  public final String key;

  public final String name;

  /** Class of the tracked instance. */
  public final String className;

  /** List of all fields (member and static) for that instance. */
  public final List<LeakReference> fields;

  public TrackedReference(String key, String name, String className, List<LeakReference> fields) {
    this.key = key;
    this.name = name;
    this.className = className;
    this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
  }
}
