import org.apache.camel.Exchange
import org.apache.camel.LoggingLevel._
import org.apache.camel.component.http4.HttpOperationFailedException
import org.apache.camel.scala.dsl.builder.RouteBuilder
import org.apache.http.conn.HttpHostConnectException
import scala.collection.JavaConversions._

/**
 * Camel route that sends JSON files to an HTTP endpoint.
 */
class JsonPost extends RouteBuilder {

  """file:target/incoming/?include=.*\.json$""" ==> {

    // Define the route ID, which is shown in the log.
    id("json")

    // Log the start of message processing.
    log("Incoming file: ${header.CamelFileName}")

    // Log an error for a failure to connect to the external web service.
    handle[HttpHostConnectException] {
      log(ERROR, "Cannot connect to the HTTP server")
      to("file:target/failed")
    }.handled

    // Log any web service errors and move the file to the failure endpoint.
    handle[HttpOperationFailedException] {
      log(ERROR, "HTTP response status: ${exception.statusCode} ${exception.statusText}")
      log(DEBUG, "HTTP response body:\n${exception.responseBody}")
      to("file:target/failed")
    }.handled

    // Send the message by HTTP POST and log the response from the server.
    setHeader(Exchange.CONTENT_TYPE, "application/json")
    to("http4:localhost:9000/")
    log("HTTP response status: ${header.CamelHttpResponseCode}")
    log(DEBUG, "HTTP response body:\n${body}")
  }
}
