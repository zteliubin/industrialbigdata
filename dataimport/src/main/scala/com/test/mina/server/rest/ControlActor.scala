package com.test.mina.server.rest

import akka.actor.{Actor, ActorRefFactory}
import com.test.mina.server.auth.{Auth, BlackListProc}
import com.test.mina.server.sessionManger.ManagerSession
import com.test.mina.server.utils.{LogSupport, SendDataStruct}
import org.json4s.{DefaultFormats, Formats}
import spray.httpx.Json4sSupport
import spray.routing.HttpService
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

class ControlActor extends Actor with HttpService with Json4sSupport with LogSupport {

  override def actorRefFactory: ActorRefFactory = context

  implicit def json4sFormats: Formats = DefaultFormats

  override def receive = runRoute {
    path("remove" / "machine") {
      parameters('machineid) {
        machineId => {
          logInfo(s"will remove machine $machineId in redis.")
          get {
            detach() {
              complete {
                Auth.removeMachine(machineId)
              }
            }
          }
        }
      }
    } ~
      path("get" / "blacklist") {
        get {
          detach() {
            complete {
              BlackListProc.getBlackList()
            }
          }
        }
      } ~
      path("send") {
        post {
          entity(as[SendDataStruct]) {
            sendCommand => {
              detach() {
                complete {
                  ManagerSession.send(sendCommand.machineId, sendCommand.value)
                }
              }
            }
          }

        }
      }
  }

}
