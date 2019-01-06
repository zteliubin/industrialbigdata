package scala.com.bigdata

import redis.clients.jedis.{Jedis, JedisPool}
import org.apache.commons.pool2.impl.GenericObjectPoolConfig

object RedisClient  extends  Serializable {

  private var pool: JedisPool = _

  def makePool(redisHost: String, redisPort: Int, redisTimeout: Int,
               maxTotal: Int, maxIdle: Int, minIdle: Int, dataBase: Int): Unit = {
    makePool(redisHost, redisPort, redisTimeout, maxTotal, maxIdle,
      minIdle, true, false, 10000, dataBase)
  }

  def makePool(redisHost: String, redisPort: Int, redisTimeout: Int,
               maxTotal: Int, maxIdle: Int, minIdle: Int, testOnBorrow: Boolean,
               testOnReturn: Boolean, maxWaitMillis: Long, dataBase: Int = 0): Unit = {
    if (pool == null) {
      val poolConfig = new GenericObjectPoolConfig()
      poolConfig.setMaxTotal(maxTotal)
      poolConfig.setMaxIdle(maxIdle)
      poolConfig.setMinIdle(minIdle)
      poolConfig.setTestOnBorrow(testOnBorrow)
      poolConfig.setTestOnReturn(testOnReturn)
      poolConfig.setMaxWaitMillis(maxWaitMillis)
      pool = new JedisPool(poolConfig, redisHost, redisPort, redisTimeout, "yzgLH20181125")

      val hook = new Thread {
        override def run: Unit = pool.destroy()
      }
      sys.addShutdownHook(hook.run)
    }
  }

  def getPool: JedisPool = {
    assert(pool != null)
    pool
  }
}
