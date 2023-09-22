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
   * Register a lifecycle callback.
   */
  void register(Callback callback);

  /**
   * Register a lifecycle callback.
   * <p>
   * The callbacks are executed with order from low to high (0 means run first).
   * <p>
   * This will execute after the server has deemed there are no active requests.
   *
   * @param callback The lifecycle callback function
   * @param order    The relative order to execute with 0 meaning run first
   */
  void register(Callback callback, int order);

  /**
   * Register a preStart lifecycle callback.
   */
  void preStart(Runnable preStartAction, int order);

  /**
   * Register a postStart lifecycle callback.
   */
  void postStart(Runnable postStartAction, int order);

  /**
   * Register a preStop lifecycle callback.
   */
  void preStop(Runnable preStopAction, int order);

  /**
   * Register a postStop lifecycle callback.
   */
  void postStop(Runnable postStopAction, int order);
}
