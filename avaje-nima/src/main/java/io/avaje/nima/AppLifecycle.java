package io.avaje.nima;

/**
 * Application lifecycle support.
 */
public interface AppLifecycle {

  enum Status {
    STARTING,
    STARTED,
    STOPPING,
    STOPPED
  }

  interface Event {
    Status status();
  }

  interface Callback {
    default void preStart() {
      // do nothing by default
    }

    default void postStart() {
      // do nothing by default
    }

    default void preStop() {
      // do nothing by default
    }

    default void postStop() {
      // do nothing by default
    }
  }

  /**
   * Return the current status.
   */
  Status status();

  /**
   * Return true if status starting or started (the server is coming up).
   */
  default boolean isAlive() {
    final var status = status();
    return status == Status.STARTING || status == Status.STARTED;
  }

  /**
   * Return true the server has started.
   */
  default boolean isReady() {
    final var status = status();
    return status == Status.STARTED;
  }

  /**
   * Register a Runnable to run on shutdown of the server.
   * <p>
   * This will execute after the server has deemed there are no active requests.
   */
  void register(Callback callback);

  /**
   * Register a Runnable to run on shutdown of the server with ordering.
   * <p>
   * The runnables are executed with order from low to high (0 means run first).
   * <p>
   * This will execute after the server has deemed there are no active requests.
   *
   * @param callback The lifecycle callback function
   * @param order    The relative order to execute with 0 meaning run first
   */
  void register(Callback callback, int order);
}
