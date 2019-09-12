package com.leak.sdk.leaklibrary;

/**
 * later if needed.
 */
public interface ObserverExecutor {
  ObserverExecutor NONE = new ObserverExecutor() {
    @Override
    public void execute(Retryable retryable) {
    }
  };

  void execute(Retryable retryable);
}
