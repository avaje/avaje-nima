package io.avaje.nima;

final class DCallback implements AppLifecycle.Callback {

  private static final Runnable NOOP = () -> {};

  private final Runnable preStart;
  private final Runnable postStart;
  private final Runnable preStop;
  private final Runnable postStop;

  private DCallback(Runnable preStart, Runnable postStart, Runnable preStop, Runnable postStop) {
    this.preStart = preStart;
    this.postStart = postStart;
    this.preStop = preStop;
    this.postStop = postStop;
  }

  static AppLifecycle.Callback preStart(Runnable preStartAction) {
    return new DCallback(preStartAction, NOOP, NOOP, NOOP);
  }

  static AppLifecycle.Callback postStart(Runnable postStartAction) {
    return new DCallback(NOOP, postStartAction, NOOP, NOOP);
  }

  static AppLifecycle.Callback preStop(Runnable preStopAction) {
    return new DCallback(NOOP, NOOP, preStopAction, NOOP);
  }

  static AppLifecycle.Callback postStop(Runnable postStopAction) {
    return new DCallback(NOOP, NOOP, NOOP, postStopAction);
  }

  @Override
  public void preStart() {
    preStart.run();
  }

  @Override
  public void postStart() {
    postStart.run();
  }

  @Override
  public void preStop() {
    preStop.run();
  }

  @Override
  public void postStop() {
    postStop.run();
  }
}
