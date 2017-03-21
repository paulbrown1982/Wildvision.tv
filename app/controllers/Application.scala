package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import play.api.db.Database
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, Controller}
import services.Loader
import services.Loader.wildvisionView

import play.api.libs.concurrent.Execution.Implicits.defaultContext

class Application @Inject()(implicit db: Database, system: ActorSystem, ws: WSClient) extends Controller {

  Loader.start

  def healthcheck: Action[AnyContent] = Action { request =>
    wildvisionView.lastUpdated map { u => Ok(s"OK - last updated: $u") } getOrElse ServiceUnavailable("Loading...")
  }

  def update: Action[AnyContent] = Action.async { request =>
    Loader.updateView.map(_.map { d => Ok(d.toString) } getOrElse InternalServerError("Cannot update"))
  }

}
