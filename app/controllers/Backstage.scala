package controllers

import javax.inject.Inject

import controllers.BackstageProperties.{presenterBackstagePass, privateEmailAddresKey}
import play.api.db.Database
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller, Request}
import play.twirl.api.Html
import services.Loader.{backstageService, backstageView}

class Backstage @Inject()(val messagesApi: MessagesApi)(implicit db: Database) extends Controller with I18nSupport {

  private def OkNoCache(content: Html) = Ok(content).withHeaders(("Cache-control", "no-store"))

  private def presenterFromSession(implicit request: Request[Any]) = {
    for {
      presenterId <- request.session.get(presenterBackstagePass)
      presenter <- backstageService.findPresenterById(presenterId.toInt)
    } yield presenter
  }

  val redirectToIndex = Redirect(routes.Backstage.index()).withNewSession
  val redirectToStudio = Redirect(routes.Backstage.studio())

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

  def update = Action { implicit request =>
    presenterFromSession map { presenter =>
      OkNoCache(backstageView.studioOf(presenter))
    } getOrElse {
      redirectToIndex
    }
  }
}

object BackstageProperties {
  val privateEmailAddresKey = "pea"
  val presenterBackstagePass = "pass"
}