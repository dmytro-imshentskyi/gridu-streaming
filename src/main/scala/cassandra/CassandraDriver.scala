package cassandra

import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.SparkConf

object CassandraDriver {

  val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("stop-adv-bot")

  lazy val connect = CassandraConnector(conf)

}
