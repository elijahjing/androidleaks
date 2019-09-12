package com.leak.sdk.leaklibrary;

/** A unit of work that can be retried later. */
public interface Retryable {

  enum Result {
    DONE, RETRY
  }

  Result run();
}
