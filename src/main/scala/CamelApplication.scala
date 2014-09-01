import org.apache.camel.impl.DefaultCamelContext

/**
 * Minimal Camel application that starts Camel with two routes.
 */
object CamelApplication {

  def main(args: Array[String]) = {
    val context = new DefaultCamelContext()
    context.setUseMDCLogging(true)
    context.addRoutes(new JsonPost())
    context.addRoutes(new FileUpload())
    context.start()
    Thread.currentThread.join()
  }
}
