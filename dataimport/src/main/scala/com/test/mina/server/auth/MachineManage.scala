package com.test.mina.server.auth

import com.test.mina.server.redis.RedisClient
import com.test.mina.server.utils.{ConstDefine, LogSupport}
import collection.JavaConverters._

object MachineManage extends LogSupport {
  val redis = RedisClient.getPool.getResource

  def insertMachine(machineId: String): Long = {
    val machineNum = redis.llen(ConstDefine.MACHINEONLINE)
    val allOnlineMachine = if (machineNum != 0 ) redis.lrange(ConstDefine.MACHINEONLINE, 0, machineNum).asScala.toList
      else Nil
    if (!allOnlineMachine.contains(machineId)) {
      redis.lpush(ConstDefine.MACHINEONLINE, machineId)
    } else {
      logWarn(s"key: [$machineId] is online already..")
      0
    }
  }

  def removeMachine(machineId: String): Long = {
    redis.lrem(ConstDefine.MACHINEONLINE, 0, machineId)
  }

  def clear(): Long = {
    redis.del(ConstDefine.MACHINEONLINE)
  }

}
