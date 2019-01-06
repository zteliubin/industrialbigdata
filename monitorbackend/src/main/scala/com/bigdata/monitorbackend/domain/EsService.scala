package com.bigdata.monitorbackend.domain

import com.bigdata.monitorbackend.config.EsConfig
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.springframework.stereotype.Service

@Service
class EsService {

  @Autowired
  private var esConfig: EsConfig = _

  /**
    * 创建EsClient
    * @return client
    */
  def createEsClient(): RestHighLevelClient = {
    new RestHighLevelClient(
      RestClient.builder(
        new HttpHost(esConfig.getHost, esConfig.getPort, "http")))
  }

  /**
    * 关闭EsClient
    * @param client es的client对象
    */
  def closeClient(client: RestHighLevelClient): Unit = {
    client.close()
  }

}
