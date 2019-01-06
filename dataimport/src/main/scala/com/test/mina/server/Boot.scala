package com.test.mina.server

import com.test.mina.server.kafka.HuaChengProducer
import com.test.mina.server.msgproc.ServerMsgProtocol
import com.test.mina.server.rest.ActorMain
import com.test.mina.server.utils.{ConfigManager, LogSupport}


object Boot extends LogSupport {
  def main(args: Array[String]): Unit = {
    logInfo("program is starting..................")
    // 1、连接kafka检查相关topic是否已经创建
    val producerObj = new HuaChengProducer()
    logInfo("step 1: try to create topic in kafka.")
    ConfigManager.topicsList.foreach(topic => {
      producerObj.createTopic(topic, 3, ConfigManager.kafkaPartitions,
        ConfigManager.kafkaReplication)
    })
    logInfo("step 2: start socket server.....")
    ServerMsgProtocol.serverStart

    // 开启rest服务
    new ActorMain()
  }
}
