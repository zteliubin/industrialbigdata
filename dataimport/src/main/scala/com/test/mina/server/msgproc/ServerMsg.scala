package com.test.mina.server.msgproc

import java.util.HashMap

import com.test.mina.server.auth.MachineManage
import com.test.mina.server.kafka.HuaChengProducer
import com.test.mina.server.utils._
import net.minidev.json.JSONObject
import org.apache.mina.core.future.{CloseFuture, IoFuture, IoFutureListener}
import org.apache.mina.core.service.IoHandler
import org.apache.mina.core.session.{IdleStatus, IoSession}


class ServerMsg extends IoHandler with LogSupport with JsonProcess {
  @throws[Exception]
  override def exceptionCaught(session: IoSession, cause: Throwable): Unit = {
    logError("server发生异常：\n" + cause.toString)
  }

  @throws[Exception]
  override def messageReceived(session: IoSession, message: Any): Unit = {
    val msg: String = message.asInstanceOf[String]
    logDebug(s"收到[${session.getRemoteAddress}/${session.getId}]数据：$msg")
    // 将收到的数据转换成json对象
    val strObj: JSONObject = try {
      parseJson(msg)
    }
    catch {
      case e: Exception => logError(s"not support not json object data. " +
        s"will close the session. \n ${e.getStackTraceString}")
        session.closeNow
        val map = new HashMap[String, Int]
        map.put("func", -1)
        new JSONObject(map)

    }
    // 检查功能码，如果是数据帧则直接将数据写入kafka
    strObj.get("func").toString.toInt match {
      case FuncDefine.REALTIMEDATA => {
        // 数据帧数据将直接推送到kafka
        val key = session.getAttribute(ConstDefine.MACHINEKEY).toString
        HuaChengProducer.sendData(ConfigManager.hcDataTopic, key, msg)
      }
      case FuncDefine.REALTIMESTATU => {
        // 状态帧也需要考虑，可能也推送到kafka
        val key = session.getAttribute(ConstDefine.MACHINEKEY).toString
        HuaChengProducer.sendStatus(ConfigManager.hcStatusTopic, key, msg)
      }
      case FuncDefine.AGPSINFO => {
        // GPS信息将直接推送到kafka进行保存处理
        val key = session.getAttribute(ConstDefine.MACHINEKEY).toString
        HuaChengProducer.sendAgps(ConfigManager.hcGpsTopic, key, msg)
      }
      case FuncDefine.REMOTECONTROLRESPONSE => {
        // 服务器给网关发送请求后的回复数据，需要在该程序中直接处理
      }
      case FuncDefine.STATEONEFUNC => {
        // 第一段实现发送给服务器的数据帧
      }
      case _ => // 不存在的命令
        logWarn(s"not support this command: ${strObj.get("func").toString.toInt}")
    }
  }


  @throws[Exception]
  override def messageSent(session: IoSession, message: Any): Unit = {
    logDebug(s"send message [${message.asInstanceOf[String]}] to [${session.getRemoteAddress}/${session.getId}]")
    // 发送消息的一些回调处理
  }

  @throws[Exception]
  override def sessionClosed(session: IoSession): Unit = {
    logDebug(s"关闭: ${session.getRemoteAddress}/${session.getId}")
    try {
      val machineId = session.getAttribute(ConstDefine.MACHINEKEY).toString
      MachineManage.removeMachine(machineId)
    } catch {
      case e: Exception => logError(s"${e.getMessage} \n ${e.getStackTraceString}")
    } finally {
      val closeFuture = session.closeNow
      closeFuture.addListener(new IoFutureListener[IoFuture]() {
        override def operationComplete(future: IoFuture): Unit = {
          if (future.isInstanceOf[CloseFuture]) {
            future.asInstanceOf[CloseFuture].setClosed()
            logInfo("sessionClosed CloseFuture setClosed-->" + future.getSession.getId)
          }
        }
      })
    }
  }

  @throws[Exception]
  override def sessionCreated(session: IoSession): Unit = {
    logInfo("创建一个新连接：" + session.getRemoteAddress + "  id:  " + session.getId)
  }

  @throws[Exception]
  override def inputClosed(ioSession: IoSession): Unit = {
    logInfo("关闭连接：" + ioSession.getRemoteAddress + "  id:  " + ioSession.getId)
    try {
      val machineId = ioSession.getAttribute(ConstDefine.MACHINEKEY).toString
      MachineManage.removeMachine(machineId)
    } catch {
      case e: Exception => logError(s"${e.getMessage} \n ${e.getStackTraceString}")
    } finally {
      ioSession.closeNow()
    }
  }

  @throws[Exception]
  override def sessionIdle(session: IoSession, status: IdleStatus): Unit = {
    logInfo("当前连接处于空暇状态：" + session.getRemoteAddress + status)
  }

  @throws[Exception]
  override def sessionOpened(session: IoSession): Unit = {
    logInfo("打开一个session id：" + session.getId + "  空暇连接个数IdleCount：  " + session.getBothIdleCount)
  }
}

