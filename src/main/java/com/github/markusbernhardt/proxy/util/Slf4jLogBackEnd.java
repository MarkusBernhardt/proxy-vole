package com.github.markusbernhardt.proxy.util;

import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*****************************************************************************
 * Slf4j logging backend.
 ****************************************************************************/

public class Slf4jLogBackEnd implements Logger.LogBackEnd {

  protected Map<Class<?>, org.slf4j.Logger> loggers = new ConcurrentHashMap<Class<?>, org.slf4j.Logger>();

  /**
   * {@inheritDoc}
   */
  @Override
  public void log(Class<?> clazz, Logger.LogLevel loglevel, String msg, Object... params) {
    org.slf4j.Logger log = getLogger(clazz);

    switch (loglevel) {
    case ERROR:
      if (log.isErrorEnabled()) {
        log.error(msg, params);
      }
      break;
    case WARNING:
      if (log.isWarnEnabled()) {
        log.warn(msg, params);
      }
      break;
    case INFO:
      if (log.isInfoEnabled()) {
        log.info(msg, params);
      }
      break;
    case TRACE:
      if (log.isTraceEnabled()) {
        log.trace(msg, params);
      }
      break;
    case DEBUG:
      if (log.isDebugEnabled()) {
        log.debug(msg, params);
      }
      break;
    }
  }

  protected org.slf4j.Logger getLogger(Class<?> clazz) {
    org.slf4j.Logger logger = loggers.get(clazz);
    if (logger == null) {
      logger = LoggerFactory.getLogger(clazz);
      loggers.put(clazz, logger);
    }
    return logger;
  }
}
