package com.bigdata.monitorbackend

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = Array("com.bigdata.monitorbackend"))
class MonitorBackendApplication {

}

object MonitorBackendApplication extends App{
	SpringApplication.run(classOf[MonitorBackendApplication])
}


