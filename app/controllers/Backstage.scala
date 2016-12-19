package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import controllers.BackstageProperties.{presenterBackstagePass, privateEmailAddresKey}
import play.api.db.Database
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller, Request}
import play.twirl.api.Html
import services.Loader.{backstageService, backstageView, updateView}
import views.backstage.BackstageView

class Backstage @Inject()(val messagesApi: MessagesApi)(implicit db: Database, system: ActorSystem, ws: WSClient) extends Controller with I18nSupport {

  private def OkNoCache(content: Html) = Ok(content).withHeaders(("Cache-control", "no-store"))

  private def presenterFromSession(implicit request: Request[Any]) = {
    for {
      presenterId <- request.session.get(presenterBackstagePass)
      presenter <- backstageService.findPresenterById(presenterId.toInt)
    } yield presenter
  }

  private val redirectToIndex = Redirect(routes.Backstage.index()).withNewSession
  private val redirectToStudio = Redirect(routes.Backstage.studio())

  def index = Action { request =>
    request.session.get(presenterBackstagePass) map { pass =>
      redirectToIndex
    } getOrElse {
      Ok(backstageView.login)
    }
  }

  def login = Action { request =>
    val result = for {
      fields <- request.body.asFormUrlEncoded
      emailAddress <- fields.get(privateEmailAddresKey).flatMap(_.headOption)
      presenter <- backstageService.findPresenterByEmail(emailAddress)
    } yield {
      redirectToStudio.withSession((presenterBackstagePass, presenter.presenter_id.toString))
    }
    result getOrElse Ok(backstageView.failedLogin(Some("Please enter a valid email address")))
  }

  def logout = Action { request =>
    redirectToIndex
  }

  def studio = Action { implicit request =>
    presenterFromSession map { presenter =>
      OkNoCache(backstageView.studioOf(presenter))
    } getOrElse {
      redirectToIndex
    }
  }

  def updatePresenter() = Action { implicit request =>
    presenterFromSession map { presenter =>
      val presenterForm = BackstageView.presenterForm.bindFromRequest()
      val result = for {
        presenterFromForm <- presenterForm.value
      } yield {
        if (presenter.presenter_id == presenterFromForm.presenter_id) {
          backstageService.updatePresenter(presenterFromForm)
          updateView
          redirectToStudio
        } else {
          OkNoCache(backstageView.studioOf(presenter))
        }
      }
      result getOrElse {
        presenterForm.errors.foreach { error =>
          println(error)  // TOOD
        }
        Ok(backstageView.studioOf(presenter))
      }
    } getOrElse redirectToIndex
  }

  def updateFilm() = Action { implicit request =>
    presenterFromSession map { presenter =>
      val filmForm = BackstageView.filmForm.bindFromRequest()
      val result = for {
        filmFromForm <- filmForm.value
      } yield {
        if (backstageService.presenterOwnsFilm(presenter.presenter_id, filmFromForm.film_id)) {
          backstageService.updatePresenterFilm(filmFromForm)
          updateView
          redirectToStudio
        } else {
          OkNoCache(backstageView.studioOf(presenter))
        }
      }
      result getOrElse {
        filmForm.errors.foreach { error =>
          println(error)  // TOOD
        }
        Ok(backstageView.studioOf(presenter))
      }
    } getOrElse redirectToIndex
  }
}

object BackstageProperties {
  val privateEmailAddresKey = "pea"
  val presenterBackstagePass = "pass"
}