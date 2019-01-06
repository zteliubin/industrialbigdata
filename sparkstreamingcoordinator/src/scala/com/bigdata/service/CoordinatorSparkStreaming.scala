package scala.com.bigdata.service

import scala.com.bigdata.common.{JsonOperation, ZookeeperService, javaClassOperation}
import scala.com.bigdata.log.LogSupport
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Assign
import scala.com.bigdata.{Context, StreamingComputeBase, streamingJson}

object CoordinatorSparkStreaming  extends LogSupport with JsonOperation{

  /**
    * 获取流任务的json配置
    * @param args 参数
    * @return StreamingJson
    */
  def getStreamingJson(args: Array[String]): streamingJson = {
    val jsonContent = getJsonFileContent(args)
    logInfo(s"the jsonContent: ${jsonContent}")
    json2CaseClass[streamingJson](jsonContent)
  }

  def main(args: Array[String]): Unit = {

    val streamingConfig: streamingJson = getStreamingJson(args)
    var kafkaStream: InputDStream[ConsumerRecord[String, String]] = null
    val conf = new SparkConf().setAppName(s"${streamingConfig.topicName}_streaming_consumer").
      set("es.nodes", streamingConfig.esInfo.host).
      set("es.port", streamingConfig.esInfo.port).
      set("es.index.auto.create", "true")

    val brokerName = streamingConfig.brokers.head.split(":")(0)
    val className = streamingConfig.className


    val ssc = new StreamingContext(conf, Seconds(1))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> streamingConfig.brokers.head,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "streaming_consumer_group",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array(streamingConfig.topicName)
    val zkInstance = ZookeeperService(streamingConfig.topicName, streamingConfig.zkQuorum.head + "/kafka")

    /** 从zk获取上一次保存的patition的offset,这里需要考虑最大值与最小值越界 */
    val fromOffsets = zkInstance.calculateCunsumerOffset(brokerName, 9092)

    /**
      * 对于offset为0的情况则从头开始消费,否则从指定偏移消费
      */
    if (fromOffsets.isEmpty) {
      logInfo(s"the begin offset +++++++++++++++++++++++: fromOffsets: ${fromOffsets}")
      kafkaStream = KafkaUtils.createDirectStream[String, String](
        ssc,
        PreferConsistent,
        Subscribe[String, String](topics, kafkaParams))
    } else {
      logInfo(s"the middle offset +++++++++++++++++++++++ :fromOffsets:${fromOffsets}")
      kafkaStream = KafkaUtils.createDirectStream[String, String](
        ssc,
        PreferConsistent,
        Assign[String, String](fromOffsets.keys.toList, kafkaParams, fromOffsets))
    }

    /** 获取流计算实例 */
    val streamingComputeInst: StreamingComputeBase = javaClassOperation.loadOneInstance(className)

    /** 执行流计算实例  */
    streamingComputeInst.action(kafkaStream, Context(streamingConfig))

    /**
      *  更新Rdd每个分区的偏移到zk
      */
    kafkaStream.foreachRDD(kafkaRdd => {
      zkInstance.savePartitionOffsets(kafkaRdd)
    })

    ssc.start()
    ssc.awaitTermination()

  }
}
