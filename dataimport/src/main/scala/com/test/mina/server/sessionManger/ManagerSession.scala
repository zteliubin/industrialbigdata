package com.test.mina.server.sessionManger

import com.test.mina.server.msgproc.ServerMsgProtocol
import com.test.mina.server.utils.ConstDefine
import org.apache.mina.core.session.IoSession

import collection.JavaConverters._
import scala.collection.mutable

object ManagerSession {

  def getAllSession: mutable.Map[java.lang.Long, IoSession] = {
    if (null != ServerMsgProtocol.getAcceptor) {
      ServerMsgProtocol.getAcceptor.getManagedSessions.asScala
    } else null
  }

  def findSession(machineId: String) : IoSession = {
    val allSessions = getAllSession
    if (allSessions == null) return null
    allSessions.values.foreach(sess => {
      if(sess.getAttribute(ConstDefine.MACHINEKEY) == machineId) return sess
    })
    null // 如果没有匹配上则返回null
  }

  def send(machineId: String, value: String): String = {
    val session: IoSession = findSession(machineId)
//    val sendValue = if(value.endsWith(ConstDefine.SEP)) value else value + ConstDefine.SEP
    if(session != null) {
      if (session.write(value).isWritten) {
        "success"
      } else "fail"
    } else "null"
  }

}
