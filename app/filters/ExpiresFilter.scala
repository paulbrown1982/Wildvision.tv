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

  private val RFC1123_DATE_TIME_FORMATTER = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZoneUTC()

  override def apply(nextFilter: RequestHeader => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {

    nextFilter(requestHeader) map { result =>
      if (isEligible(requestHeader, result.header)) resultWithLastUpdated(result) else result
    }
  }

  private def resultWithLastUpdated(result: Result) = {
    wildvisionView.lastUpdated map { lastUpdated =>
      result withHeaders "Expires" -> lastUpdated.plusSeconds(interval).toString(RFC1123_DATE_TIME_FORMATTER)
    } getOrElse result
  }

  private def isEligible(requestHeader: RequestHeader, responseHeader: ResponseHeader) = {
    isGETorOPTIONS(requestHeader) && responseHeader.status == 200 && !responseHeader.headers.contains("Cache-Control")
  }

  private def isGETorOPTIONS(requestHeader: RequestHeader) =
    requestHeader.method == "OPTIONS" || requestHeader.method == "GET"


}
