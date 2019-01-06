package scala.com.bigdata.common

import scala.com.bigdata.log.LogSupport
import scala.com.bigdata.StreamingComputeBase
object javaClassOperation  extends LogSupport{

  def loadOneInstance(className: String): StreamingComputeBase = {
    try {
      val clazz = Class.forName(className)
      val cons = clazz.getConstructors
      val newInst = cons(0).newInstance()
      newInst.asInstanceOf[StreamingComputeBase]
    } catch {
      case e: ClassNotFoundException =>
        logError(s"the className: ${className} can not find")
        throw new ClassNotFoundException(s"the className: ${className} can not find")
      case e: SecurityException =>
        logError(s"get getConstructors ${className} failed")
        throw new SecurityException(s"get getConstructors ${className} failed")
      case ex: Exception => {
        logError("the exception!", ex)
        throw ex
      }
    }
  }

}
