package scala.com.bigdata.common

import java.io.{File, FileInputStream, IOException}

import org.json4s.jackson.Serialization

import scala.com.bigdata.log.LogSupport
import org.json4s.DefaultFormats
trait JsonOperation extends LogSupport{
  implicit val formats = DefaultFormats
  protected def serialization = Serialization

  /**
    * 字符串转换成case class
    * @param data 字符串数据
    * @tparam T object类型
    * @return T类型的class
    */
  def json2CaseClass[T: Manifest](data: String) = {
    try serialization.read[T](data)
    catch {
      case ex: Exception => {
        logError("the json2CaseClass exception!", ex)
        throw new Exception(s"the json2CaseClass exception, data: ${data}")
      }
    }
  }

  /**
    * 从json文件中读取文件内容
    * @param args main函数参数
    * @return 文件内容
    */
  def getJsonFileContent(args: Array[String]): String = {
    val index = args.indexOf("--config")
    if(index >=0 && args.size >= index + 1) {
      val fileName = args(index + 1)
      readContentFromFile(fileName)
    } else ""
  }

  /**
    * 读取文件内容
    * @param path 文件路径
    * @return 内容
    */
  def readContentFromFile(path: String): String = {
    logInfo(s"the path: ${path}")
    val file: File = new File(path)
    var in: FileInputStream = null
    try {
      in = new FileInputStream(file)
      /** 一次性读取所有内容 */
      val size: Int = in.available()
      val buffer: Array[Byte] = new Array[Byte](size)
      in.read(buffer)
      new String(buffer, "GB2312")
    } catch {
      case ex: Exception => throw new IOException(s"read file ${path} failed")
    } finally {
      if(in != null) {
        in.close()
      }
    }
  }

}
