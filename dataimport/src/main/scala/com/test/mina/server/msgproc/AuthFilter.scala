package com.test.mina.server.msgproc

import java.util.HashMap

import com.test.mina.server.auth.{Auth, BlackListProc, MachineManage}
import com.test.mina.server.utils.{ConstDefine, FuncDefine, JsonProcess, LogSupport}
import net.minidev.json.JSONObject
import org.apache.mina.core.filterchain.IoFilter.NextFilter
import org.apache.mina.core.filterchain.IoFilterAdapter
import org.apache.mina.core.session.IoSession
import org.apache.mina.core.write.WriteRequest


/*
    自定义客户端过滤器
 */
class AuthFilter extends IoFilterAdapter with LogSupport with JsonProcess{

  override def messageReceived(nextFilter: NextFilter, session: IoSession, message: Object) {
    val msg = message.asInstanceOf[String]
    logInfo(s"ClientFilter接收到客户端消息: [$message]")
    // 如果是空数据则直接返回丢掉报文
    if (msg.trim.isEmpty) {
      return
    }
    // 传给下一个过滤器, 此处需要鉴权，如果鉴权成功将数据传输到下一个filter，
    // 否则在此处将终止向后传递报文，并将关闭session连接
    if(session.getAttribute("authed") == "OK") {
      logInfo("this session is auth ok.")
      nextFilter.messageReceived(session, message)
    } else {
      val strObj: JSONObject = try {
        parseJson(msg)
      }
      catch {
        case e: Exception => logError(s"not support not json object data. " +
          s"will close the session. \n ${e.getStackTraceString}")
          session.closeNow
          val map = new HashMap[String , Int]
          map.put("func", -1)
          new JSONObject(map)
      }
      val machineId = if (strObj.getAsNumber("func").toString.toInt != FuncDefine.STATEONEFUNC &&
        strObj.getAsNumber("func").toString.toInt != -1) {
        val gID = strObj.getAsString("gid")
        val cID = strObj.getAsNumber("cid").toString
        s"$gID/$cID"
      } else if(strObj.getAsNumber("func").toString.toInt == -1) {
        "nonValid" // 非法
      } else {
        // TODO
        ""
      }
      if (machineId == "") {
        logWarn("machineId is empty.")
      } else if(Auth.authMachine(machineId)) {
        logInfo("auth with redis, and auth ok.")
        session.setAttribute("authed", "OK")
        session.setAttribute(ConstDefine.MACHINEKEY, machineId)
        MachineManage.insertMachine(machineId)  // 添加到redis的online列表中
        nextFilter.messageReceived(session, message)
      } else {
        // 将当前Ip添加到黑名单
        logWarn(s"auth failed for machine: {$machineId}, will closed this session[${session.getRemoteAddress.toString}].")
        session.write("not auth.")
        BlackListProc.addBlackList(session.getRemoteAddress.toString.split(":").head.tail)
        // 消息将不再向下传递，session将将进行关闭处理
        session.closeNow
        val map = new HashMap[String , Int]
        map.put("func", -1)
        new JSONObject(map)
      }
    }
  }

  override def messageSent(nextFilter: NextFilter, session: IoSession, writeRequest: WriteRequest) {
    nextFilter.messageSent(session, writeRequest)
  }
}

