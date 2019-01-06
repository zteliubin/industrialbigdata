package scala.com.bigdata

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream

case class mysqlConfig(host: String, port: Int, user: String, passwd: String, driver: String,
                       dbName: String, initialSize: Int, maxAction: Int, maxIdle: Int, maxWait: Int)
case class redisConfig(host: String, port: Int, timeout: Int, maxTotal: Int, maxIdle: Int, minIdle: Int, database: Int)
case class esConfig(host: String, port: String)
case class streamingJson(brokers: List[String], zkQuorum: List[String], topicName: String, className: String,
                         mysqlInfo: mysqlConfig, redisInfo: redisConfig, esInfo: esConfig) extends Serializable
case class Context(streamingInfo: streamingJson)

trait StreamingComputeBase extends Serializable {

  /**
    * 处理sparkstreaming计算
    * @param input    数据流
    * @param context  流计算的上下文参数
    */
  def action(input: InputDStream[ConsumerRecord[String, String]], context: Context): Unit

}
