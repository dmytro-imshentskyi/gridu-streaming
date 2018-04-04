import cassandra.CassandraSinkForeach
import com.typesafe.config.ConfigFactory
import domain.Event
import org.apache.spark.sql.functions.{from_json, from_unixtime, _}
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import scala.concurrent.duration._

object Demo {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("stop-adv-bot").master("local[*]").getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    import spark.implicits._

    val config = ConfigFactory.load()
    val kafkaUrl = config.getString("adv.bot.kafka.url")
    val kafkaTopic = config.getString("adv.bot.kafka.topic")
    val threshold = config.getInt("adv.bot.threshold")
    val eventDelayTtl = config.getInt("adv.bot.event.delay").seconds.toString
    val windowTtl = config.getInt("adv.bot.event.window").seconds.toString
    val checkpointLocalFilePath = config.getString("adv.bot.checkpoint.path")

    val df = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaUrl)
      .option("subscribe", kafkaTopic)
      .option("startingOffsets", "earliest")
      .load()

    val events = enrichWithWatermark(transformToEventDataSet(df), eventDelayTtl)

    val ipAndWindowCount = events
      .groupBy($"ip", window($"timestamp", windowTtl)).count()

    val result = ipAndWindowCount.filter($"count" >= threshold).select($"ip", $"count").as[(String, Long)]

    val queryHandler = result.writeStream
      .option("truncate", value = false)
      .option("checkpoint", checkpointLocalFilePath)
      .outputMode(OutputMode.Update())
      .foreach(new CassandraSinkForeach()).start()

    queryHandler.awaitTermination()

    spark.stop()
  }

  def enrichWithWatermark(ds: Dataset[Event], timeDelay: String): Dataset[Event] =
    ds.withWatermark("timestamp", timeDelay)

  def transformToEventDataSet(df: DataFrame): Dataset[Event] = {
    import df.sparkSession.implicits._

    val schema = StructType(
      Seq(
        StructField("type", StringType, nullable = false),
        StructField("ip", StringType, nullable = false),
        StructField("unix_time", StringType, nullable = false),
        StructField("url", StringType, nullable = false)
      ))

    df.select(from_json($"value".cast("string"), schema).as("event"))
      .select($"event.type".as("eventType"), $"event.ip".as("ip"),
        from_unixtime($"event.unix_time").cast("timestamp").as("timestamp"), $"event.url".as("url")).as[Event]
  }

}
