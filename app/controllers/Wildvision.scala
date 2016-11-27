package controllers

import javax.inject.Inject

import models.Slug._
import play.api.db.Database
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller, Cookie}
import play.twirl.api.Html
import services.InsightService._
import services.Loader.wildvisionView
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class Wildvision @Inject()(implicit db: Database, ws: WSClient) extends Controller {

  def index = Action { request =>
    wildvisionView.editorsChoiceFilm.map { f => Ok(f) } getOrElse Redirect("/films", TEMPORARY_REDIRECT)
  }

  def about = Action { request =>
    Ok(wildvisionView.about)
  }

  def cookies = Action { request =>
    val doNotTrack = request.headers.get("DNT").mkString == "1"
    val optedOut = doNotTrack || request.cookies.get("optout").exists(_.value == "true")
    Ok(wildvisionView.dynamicView.cookies(optedOut, doNotTrack))
  }

  def cookiesOptIn = Action { request =>
    Redirect(routes.Wildvision.cookies()).withCookies(Cookie("optout", "false"))
  }

  def cookiesOptOut = Action { request =>
    Redirect(routes.Wildvision.cookies()).withCookies(Cookie("optout", "true"))
  }

  def films = Action { request =>
    Ok(wildvisionView.films)
  }

  def filmsTaggedWith(tag: String) = Action { request =>
    Ok(wildvisionView.dynamicView.filmsWithTag(Some(tag)))
  }

  def presenters = Action { request =>
    Ok(wildvisionView.presenters)
  }

  def presenter(slugString: String) = Action { request =>
    wildvisionView.getPresenter(fromSlugString(slugString)) map { p => Ok(p) } getOrElse NotFound(wildvisionView.error404)
  }

  def film(slugString: String) = Action { request =>
    wildvisionView.getFilm(fromSlugString(slugString)) map { f => Ok(f) } getOrElse NotFound(wildvisionView.error404)
  }

  def filmsTagged = Action { request =>
    Ok(wildvisionView.filmsTagged)
  }

  def top10MostPopularFilms = Action { request =>
    Ok(wildvisionView.top10MostPopularFilms)
  }

  def top10MostRecentFilms = Action { request =>
    Ok(wildvisionView.top10MostRecentFilms)
  }

  def top10TrendingFilms = Action { request =>
    Ok(wildvisionView.top10TrendingFilms)
  }

  def top10LongestFilms = Action { request =>
    Ok(wildvisionView.top10LongestFilms)
  }

  def top10ShortestFilms = Action { request =>
    Ok(wildvisionView.top10ShortestFilms)
  }

  def subscribe = Action { request =>
    Ok(wildvisionView.subscribe)
  }

  def subscribeThankYou = Action { request =>
    Ok(wildvisionView.subscribeThankYou)
  }

  def newsletter(slugString: String) = Action { request =>
    wildvisionView.getNewsletter(fromSlugString(slugString)) map { n => Ok(n) } getOrElse NotFound(wildvisionView.error404)
  }

  def sitemap = Action { request =>
    Ok(wildvisionView.sitemap)
  }

  def search(q: Option[String]) = Action { request =>
    Ok(wildvisionView.dynamicView.search(q))
  }

  def privacy = Action { request =>
    Ok(wildvisionView.privacy)
  }

  def notFound(pathParts: String) = Action { request =>
    request.headers.get("Accept").filter(_.contains("text/html")).map { r =>
      NotFound(wildvisionView.error404)
    } getOrElse {
      NotFound("")
    }
  }

  def blogHomepage = Action { request =>
    Ok(wildvisionView.blogHomepage)
  }

  def blog(path: String) = Action.async { request =>
    ws.url("http://localhost:9000/healthcheck").get().map(x => Ok(Html(x.body)))
  }

  def insights(entity: String, insightType: String, entityId: String) = Action { request =>
    Try(entityId.toInt).toOption map { id =>
      val resp = record(entity, insightType, id, request.headers.get("IP").getOrElse("127.0.0.1"))
      resp.map(i => Ok(i.toString)).getOrElse(InternalServerError("Not ready"))
    } getOrElse BadRequest("Invalid or missing 'id' form data value")
  }
}
