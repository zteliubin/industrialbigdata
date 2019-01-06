package com.test.mina.server.rest

import com.test.mina.server.utils.{ConfigManager, LogSupport}
import spray.servlet.WebBoot
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.io.IO
import spray.can.Http

class ActorMain extends WebBoot with LogSupport{
  implicit def system: ActorSystem = DataImportSystem.actorSystem

  override def serviceActor: ActorRef = system.actorOf(Props(new DataImportActor)) // 实例化所有的actor

  system.registerOnTermination {
    system.terminate()
  }

  IO(Http) ! Http.Bind(serviceActor, ConfigManager.restHost, ConfigManager.restPort)

}

object DataImportSystem {
  implicit  val actorSystem = ActorSystem("data-import-rest")
}
