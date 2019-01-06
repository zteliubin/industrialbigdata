package com.bigdata.monitorbackend.controller

import java.util
import java.util.Date

import com.bigdata.monitorbackend.domain.EsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{RequestMapping, RestController}

@RestController
@RequestMapping(Array("/hello"))
class EsController {

  @Autowired
  var service: EsService = _

  @RequestMapping(Array("", "/"))
  def greeting() = {
    val now = new Date
    val content = "Hello, ! Now is: " + now
    val json = new util.HashMap[String, String]
    json.put("content", content)
    json
  }
}
