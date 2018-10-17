package com.github.markusbernhardt.proxy.util;

/*****************************************************************************
 * Simple logging support for the framework. You need to add a logging listener
 * that needs to send the logging events to a backend.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class Logger {

  public enum LogLevel {
    ERROR, WARNING, INFO, TRACE, DEBUG
  }

  /*****************************************************************************
   * Interface for a logging backend that can be attached to the logger.
   ****************************************************************************/

  public interface LogBackEnd {

    /*************************************************************************
     * Invoked for every logging event.
     * 
     * @param clazz
     *          the class that sends the log message.
     * @param loglevel
     *          the logging level.
     * @param msg
     *          the message format string.
     * @param params
     *          the message parameters for the format string.
     ************************************************************************/

    public void log(Class<?> clazz, LogLevel loglevel, String msg, Object... params);

  }

  private static LogBackEnd backend;

  /*************************************************************************
   * Gets the currently attached logging backend.
   * 
   * @return Returns the backend.
   ************************************************************************/

  public static LogBackEnd getBackend() {
    return backend;
  }

  /*************************************************************************
   * Attaches a new logging backend replacing the existing one.
   * 
   * @param backend
   *          The backend to set.
   ************************************************************************/

  public static void setBackend(LogBackEnd backend) {
    Logger.backend = backend;
  }

  /*************************************************************************
   * Logs a message.
   * 
   * @param clazz
   *          the class that sends the log message.
   * @param loglevel
   *          the logging level.
   * @param msg
   *          the message format string.
   * @param params
   *          the message parameters for the format string.
   ************************************************************************/

  public static void log(Class<?> clazz, LogLevel loglevel, String msg, Object... params) {
    if (backend == null) {
      backend = new Slf4jLogBackEnd();
    }
    backend.log(clazz, loglevel, msg, params);
  }

}
