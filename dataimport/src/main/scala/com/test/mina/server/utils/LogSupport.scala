package com.test.mina.server.utils

import org.slf4j.LoggerFactory

/**
  * Created by Administrator on 2018/11/20.
  */
trait LogSupport {
  private val log = LoggerFactory.getLogger(this.getClass)

  protected def logDebug(msg: => String): Unit ={
    if(log.isDebugEnabled) {
      log.debug(msg)
    }
  }

  protected def logInfo(msg: => String): Unit ={
    if(log.isInfoEnabled) {
      log.info(msg)
    }
  }

  protected def logError(msg: => String, exception: Throwable = null): Unit ={
    if(log.isErrorEnabled) {
      log.error(msg, exception)
    }
  }

  protected def logWarn(msg: => String, exception: Throwable = null): Unit ={
    if(log.isWarnEnabled) {
      log.warn(msg, exception)
    }
  }
}
