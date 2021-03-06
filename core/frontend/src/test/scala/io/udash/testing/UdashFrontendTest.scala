package io.udash.testing

import com.github.ghik.silencer.silent
import org.scalajs.dom
import org.scalatest.{Assertion, Succeeded}
import org.scalatest.concurrent.PatienceConfiguration

import scala.concurrent.{Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext
import scala.scalajs.js.Date
import scala.util.{Failure, Success}

trait FrontendTestUtils {
  import scalatags.JsDom.all.div
  def emptyComponent() = div().render

  @silent
  implicit val testExecutionContext = JSExecutionContext.runNow
}

trait UdashFrontendTest extends UdashSharedTest with FrontendTestUtils
trait AsyncUdashFrontendTest extends AsyncUdashSharedTest with FrontendTestUtils with PatienceConfiguration {
  def eventually(code: => Any)(implicit patienceConfig: PatienceConfig): Future[Assertion] = {
    val start = Date.now()
    val p = Promise[Assertion]
    def startTest(): Unit = {
      dom.window.setTimeout(() => {
        if (patienceConfig.timeout.toMillis > Date.now() - start) {
          try {
            code
            p.complete(Success(Succeeded))
          } catch {
            case _: Exception => startTest()
          }
        } else {
          p.complete(Failure(new NullPointerException))
        }
      }, patienceConfig.interval.toMillis)
    }
    startTest()
    p.future
  }
}