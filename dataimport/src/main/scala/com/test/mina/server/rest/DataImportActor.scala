package com.test.mina.server.rest

import akka.actor.{Actor, ActorRef, ActorRefFactory, Props}
import com.test.mina.server.utils.LogSupport
import spray.routing.HttpService

class DataImportActor extends  Actor with HttpService with LogSupport{
  logInfo("data import actor is starting........")

  val system = DataImportSystem.actorSystem

  override def actorRefFactory: ActorRefFactory = context

  val controlActor: ActorRef = system.actorOf(Props(new ControlActor)) // 实例化某一个actor

  override def receive = runRoute {
    pathPrefix("dataimport") {
      controlActor ! _
    }
  }
}
