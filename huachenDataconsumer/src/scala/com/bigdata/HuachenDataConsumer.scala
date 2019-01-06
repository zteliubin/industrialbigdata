package scala.com.bigdata

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.streaming.dstream.InputDStream
import org.elasticsearch.spark.rdd.EsSpark
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization
import scala.collection.JavaConversions._

case class huachenData(ptid: Int, cid: Int, level: Int, time: String, consume: Int, err: Int, points: Map[String, String], gid: String, func: Int)

class HuachenDataConsumer extends StreamingComputeBase with LogSupport with Serializable {
  override def action(input: InputDStream[ConsumerRecord[String, String]], context: Context): Unit = {

    val redisInfo: redisConfig = context.streamingInfo.redisInfo
    val topticName: String = context.streamingInfo.topicName


    /**
      * kafka的数据入ES
      */

    input.foreachRDD(kafkaRDD => {
      kafkaRDD.foreachPartition(partitionOfRecords => {

        /**
          * 创建redis连接池
          */
        RedisClient.makePool(redisInfo.host, redisInfo.port, redisInfo.timeout,
          redisInfo.maxTotal, redisInfo.maxIdle, redisInfo.minIdle, redisInfo.database)

          partitionOfRecords.foreach(record => {
            implicit val formats = DefaultFormats
            val line = record.value()
            val jedis = RedisClient.getPool.getResource
            try {
              val data: huachenData = Serialization.read[huachenData](line)

              /**
                * 写数据到redis
                */
              jedis.hmset(s"${data.gid}/${data.cid}", data.points)
            } catch {
              case e: Exception => {
                logError(s"java 解析错误!line:" + line, e)
              }
            } finally {
              if(jedis != null) {
                RedisClient.getPool.returnResource(jedis)
              }
            }
          })
      })

      /**
        * 数据写入ES
        */
      if(!kafkaRDD.isEmpty()) {
        try {
          val  lines = kafkaRDD.map(_.value()).filter(_.nonEmpty)
          EsSpark.saveJsonToEs(lines, s"${topticName.toLowerCase}/docs")
        } catch {
          case e: Exception => {
            logError(s"topticName: ${topticName} to ES failed", e)
            throw new Exception(s"topticName: ${topticName} to ES failed", e)
          }
        }

      }
    })

  }
}
