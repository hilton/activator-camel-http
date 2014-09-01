import java.text.SimpleDateFormat
import java.util.Date

import org.apache.camel.Exchange
import org.apache.camel.LoggingLevel._
import org.apache.camel.component.http4.HttpOperationFailedException
import org.apache.camel.scala.dsl.builder.RouteBuilder
import org.apache.http.conn.HttpHostConnectException
import org.apache.http.entity.mime.MultipartEntityBuilder

/**
 * Camel route that uploads XML/PDF files to an HTTP endpoint.
 */
class FileUpload extends RouteBuilder {

  """file:target/incoming/?include=.*\.(xml|pdf)&keepLastModified=true""" ==> {

    id("upload")

    // Log an error for a failure to connect to the external web service.
    handle[HttpHostConnectException] {
      log(ERROR, "Cannot connect to the HTTP server")
      to("file:target/failed")
    }.handled

    // Log web service errors and move the file to the failure endpoint
    handle[HttpOperationFailedException] {
      log(ERROR, "HTTP response: ${exception.statusCode} ${exception.statusText}\n${exception.responseBody}")
      to("file:target/failed")
    }.handled

    // Transform the message into multipart/form-data.
    process(toMultipart _)

    // Send the message by HTTP POST and log the response.
    log("POST ${header.CamelFileName} to /upload")
    setHeader(Exchange.CONTENT_TYPE, "multipart/form-data")
    to("http4:localhost:9000/upload")
    log("HTTP response status: ${header.CamelHttpResponseCode}")
    log(DEBUG, "HTTP response body:\n${body}")
  }

  private def formatTimestamp(time: String): String  = {
    new SimpleDateFormat("yyyy-MM-dd") format new Date(time.toLong)
  }

  /**
   * Camel processor that converts the message file into an HTTP multi-part
   * entity (HTML form file upload). The request parameters are `file` (the
   * binary data), `name` (file name) and `date` (the file’s modification date).
   */
  private def toMultipart(exchange: Exchange): Unit = {

    // Read the incoming message…
    val file = exchange.getIn.getBody(classOf[java.io.File])
    val name: String = exchange.getIn.getHeader(Exchange.FILE_NAME, classOf[String])
    val date = {
      val fileDate = exchange.getIn.getHeader(Exchange.FILE_LAST_MODIFIED, classOf[String])
      new SimpleDateFormat("yyyy-MM-dd") format new Date(fileDate.toLong)
    }

    // Encode the file as a multipart entity…
    val entity = MultipartEntityBuilder.create()
    entity.addBinaryBody("file", file)
    entity.addTextBody("name", name)
    entity.addTextBody("date", date)

    // Set multipart entity as the outgoing message’s body…
    exchange.getOut.setBody(entity.build)
  }
}
