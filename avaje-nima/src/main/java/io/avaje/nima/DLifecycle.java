package io.avaje.nima;

import io.avaje.applog.AppLog;
import io.helidon.webserver.WebServer;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static io.avaje.nima.AppLifecycle.Status.*;

final class DLifecycle implements AppLifecycle {

  private static final System.Logger log = AppLog.getLogger("io.avaje.nima");

  private final List<CallbackOrder> callbacks = new ArrayList<>();
  private final ReentrantLock lock = new ReentrantLock();
  private volatile Status status = STARTING;

  @Override
  public void register(Callback callback) {
    register(callback, 1000);
  }

  @Override
  public void register(Callback callback, int order) {
    lock.lock();
    try {
      callbacks.add(new CallbackOrder(callback, order));
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Status status() {
    return status;
  }

  private void invokeCallbacks(AppLifecycle.Status status) {
    this.status = status;
    if (status == STARTING) {
      Collections.sort(callbacks);
    }
    for (CallbackOrder callbackOrder : callbacks) {
      try {
        switch (status) {
          case STARTING -> callbackOrder.callback.preStart();
          case STARTED -> callbackOrder.callback.postStart();
          case STOPPING -> callbackOrder.callback.preStop();
          case STOPPED -> callbackOrder.callback.postStop();
        }
      } catch (Exception e) {
        log.log(Level.ERROR, "Error running shutdown runnable", e);
        // maybe logging has stopped so also do ...
        e.printStackTrace();
      }
    }
  }

  void start(WebServer delegate) {
    lock.lock();
    try {
      invokeCallbacks(STARTING);
      delegate.start();
      invokeCallbacks(STARTED);
    } finally {
      lock.unlock();
    }
  }

  void stop(WebServer delegate) {
    lock.lock();
    try {
      if (status == STOPPED) {
        log.log(Level.INFO, "already stopped");
      } else {
        invokeCallbacks(STOPPING);
        delegate.stop();
        invokeCallbacks(STOPPED);
      }
    } finally {
      lock.unlock();
    }
  }

  static final class CallbackOrder implements Comparable<CallbackOrder> {
    private final Callback callback;
    private final int order;

    CallbackOrder(Callback callback, int order) {
      this.callback = callback;
      this.order = order;
    }

    @Override
    public int compareTo(CallbackOrder other) {
      return Integer.compare(order, other.order);
    }
  }

}
