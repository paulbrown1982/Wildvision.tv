package filters

import javax.inject._

import akka.stream.Materializer
import org.joda.time.format.DateTimeFormat
import play.api.mvc._
import services.Loader.{interval, wildvisionView}

import scala.concurrent.{ExecutionContext, Future}
/**
 * This is a simple filter that adds an Expires header to all requests. It's
 * added to the application's list of filters by the
 * [[Filters]] class.
 *
 * @param mat This object is needed to handle streaming of requests
 * and responses.
 * @param exec This class is needed to execute code asynchronously.
 * It is used below by the `map` method.
 */
@Singleton
class ExpiresFilter @Inject()(implicit override val mat: Materializer, exec: ExecutionContext) extends Filter {

  val RFC1123_DATE_TIME_FORMATTER = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZoneUTC()

  private def isGETorOPTIONS(requestHeader: RequestHeader) =
    requestHeader.method == "OPTIONS" || requestHeader.method == "GET"

  override def apply(nextFilter: RequestHeader => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {

    nextFilter(requestHeader) map { result =>
      wildvisionView.lastUpdated map { u =>
        if (!result.header.headers.contains("Cache-Control") && result.header.status == 200 && isGETorOPTIONS(requestHeader)) {
          result withHeaders "Expires" -> u.plusSeconds(interval).toString(RFC1123_DATE_TIME_FORMATTER)
        } else {
          result
        }
      } getOrElse result
    }
  }

}
