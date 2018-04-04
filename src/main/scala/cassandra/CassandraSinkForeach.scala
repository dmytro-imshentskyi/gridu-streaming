package cassandra

import java.time.{LocalDateTime, ZoneId}

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.ForeachWriter

class CassandraSinkForeach extends ForeachWriter[(String, Long)]{

  private val timeToBlockInSeconds = ConfigFactory.defaultApplication().getLong("adv.bot.ttl")

  override def open(partitionId: Long, version: Long): Boolean = true

  override def process(value: (String, Long)): Unit =
    CassandraDriver.connect.withSessionDo(_.execute(registerBotCql(value)))

  private[this] def registerBotCql(ipToCount: (String, Long)) = {
    val zone = ZoneId.systemDefault()

    val startTime = LocalDateTime.now().atZone(zone).toEpochSecond
    val endTime = startTime + timeToBlockInSeconds

    s"""
      INSERT INTO adv.ip_event_count (ip, count, startTime, endTime)
      VALUES('${ipToCount._1}', ${ipToCount._2}, $startTime, $endTime)
    """
  }

  override def close(errorOrNull: Throwable): Unit = ()

}
