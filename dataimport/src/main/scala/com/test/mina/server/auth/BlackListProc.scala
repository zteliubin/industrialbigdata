package com.test.mina.server.auth
import java.net.InetAddress

import com.test.mina.server.redis.RedisClient
import com.test.mina.server.utils.{ConfigManager, ConstDefine}
import org.apache.mina.filter.firewall.BlacklistFilter

/**
  * 为了不影响性能，暂时不考虑加锁
  */
object BlackListProc {
  lazy val blackList = new BlacklistFilter()
  var blackListSet: Set[(String, Long)] = Set()
  setBlackList()

  private def setBlackList(): Unit = {
    val addrObj: Array[InetAddress] = blackListSet.map(address => {
      InetAddress.getByName(address._1)
    }).toArray
    blackList.setBlacklist(addrObj)
  }

  def addBlackList(address: String): Unit = {
    blackListSet += ((address, System.currentTimeMillis)) // 先添加再设置
    setBlackList()
    removeTimeOutAddr()
  }

  private def removeTimeOutAddr(): Unit = {
    val currentTime = System.currentTimeMillis()
    blackListSet.foreach(black => {
      if (currentTime - black._2 >= ConfigManager.blackListTimeout) {
        blackListSet -= black
      }
    })
  }

  def getBlackList(): String = {
    blackListSet.map(_._1).mkString(" | ")
  }


}
