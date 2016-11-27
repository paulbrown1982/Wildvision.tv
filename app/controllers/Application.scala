package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import play.api.db.Database
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import services.Loader
import services.Loader.wildvisionView
import scala.concurrent.ExecutionContext.Implicits.global

class Application @Inject()(implicit db: Database, system: ActorSystem, ws: WSClient) extends Controller {

  Loader.start

  def healthcheck = Action { request =>
    wildvisionView.lastUpdated map { u => Ok(s"OK - last updated: $u") } getOrElse ServiceUnavailable("Loading...")
  }

  def update = Action.async { request =>
    Loader.updateView.map(_.map { d => Ok(d.toString) } getOrElse InternalServerError("Cannot update"))
  }

}
