import java.sql.Timestamp

package object domain {

  case class Event(eventType: String, ip: String, timestamp: Timestamp, url: String)

}
