package dc.utils


// a "Logger" wrapping org.slf4j.Logger
object Logger {
  def apply(name: String): org.slf4j.Logger =
    org.slf4j.LoggerFactory.getLogger(name)

  def apply(name:String, query:String): org.slf4j.Logger =
    org.slf4j.LoggerFactory.getLogger(query+name)

}
