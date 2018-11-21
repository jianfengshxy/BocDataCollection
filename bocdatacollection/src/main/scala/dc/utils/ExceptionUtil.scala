package de.utils

import java.io.{PrintWriter, StringWriter}

object ExceptionUtil {
  def getStackTrace(e: Throwable) = {
    val stringWriter = new StringWriter()
    val printWriter = new PrintWriter(stringWriter)
    e.printStackTrace(printWriter)
    stringWriter.toString
  }
}
