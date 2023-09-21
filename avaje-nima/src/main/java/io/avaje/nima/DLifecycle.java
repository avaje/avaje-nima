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

  private final List<Pair> callbacks = new ArrayList<>();
  private final ReentrantLock lock = new ReentrantLock();
  private Status status = STARTING;

  @Override
  public void register(Callback callback) {
    register(callback, 1000);
  }

  @Override
  public void register(Callback callback, int order) {
    lock.lock();
    try {
      callbacks.add(new Pair(callback, order));
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Status status() {
    return status;
  }

  void setStatus(Status newStatus) {
    status = newStatus;
  }

  private void fire(AppLifecycle.Status status) {
    setStatus(status);
    Collections.sort(callbacks);
    for (Pair pair : callbacks) {
      try {
        switch (status) {
          case STARTING -> pair.callback.preStart();
          case STARTED -> pair.callback.postStart();
          case STOPPING -> pair.callback.preStop();
          case STOPPED -> pair.callback.postStop();
        }
      } catch (Exception e) {
        log.log(Level.ERROR, "Error running shutdown runnable", e);
        // maybe logging has stopped so also do ...
        e.printStackTrace();
      }
    }
  }

  void start(WebServer delegate) {
    fire(STARTING);
    delegate.start();
    fire(STARTED);
  }

  void stop(WebServer delegate) {
    lock.lock();
    try {
      if (status == STOPPED) {
        log.log(Level.INFO, "already stopped");
      } else {
        fire(STOPPING);
        delegate.stop();
        fire(STOPPED);
      }
    } finally {
      lock.unlock();
    }
  }


  static final class Pair implements Comparable<Pair> {
    private final Callback callback;
    private final int order;

    Pair(Callback callback, int order) {
      this.callback = callback;
      this.order = order;
    }

    @Override
    public int compareTo(Pair other) {
      return Integer.compare(order, other.order);
    }
  }

}
