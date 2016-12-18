package services

import akka.actor.ActorSystem
import anorm._
import models._
import play.api.db.Database
import play.api.libs.ws.WSClient
import views.backstage.BackstageView
import views.wildvision.RenderedView

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Loader {

  private val filmParser: RowParser[Film] = Macro.namedParser[Film]
  private val presenterParser: RowParser[Presenter] = Macro.namedParser[Presenter]
  private val filmPresenterParser: RowParser[FilmPresenter] = Macro.indexedParser[FilmPresenter]
  private val configParser: RowParser[Config] = Macro.namedParser[Config]
  private val insightParser: RowParser[Insight] = Macro.namedParser[Insight]
  private val newsletterParser: RowParser[Newsletter] = Macro.namedParser[Newsletter]
  private val filmTagParser: RowParser[FilmTag] = Macro.namedParser[FilmTag]

  private var renderedView: RenderedView = new RenderedView
  var adminService: BackstageService = new BackstageService
  private var adminView: BackstageView = new BackstageView(adminService)

  def updateView(implicit db: Database, ws: WSClient) = {
    db.withConnection { implicit connection =>
      val allFilms = SQL("SELECT * FROM films").as(filmParser.*)
      val activeFilms = allFilms.filter(_.live == '1')
      val allPresenters = SQL("SELECT * FROM presenters").as(presenterParser.*)
      val activePresenters = allPresenters.filter(_.live == '1')
      val filmPresenters = SQL("SELECT * FROM film_presenters").as(filmPresenterParser.*)
      val insights = SQL("SELECT * FROM insights").as(insightParser.*)
      val config = SQL("SELECT * FROM config").as(configParser.*)
      val allNewsletters = SQL("SELECT * FROM newsletters").as(newsletterParser.*)
      val filmTags = SQL("SELECT * FROM film_tags").as(filmTagParser.*)
      renderedView = new RenderedView(
        config,
        activeFilms,
        activePresenters,
        filmPresenters,
        insights,
        allNewsletters,
        filmTags
      )
      adminService = new BackstageService(
        config,
        allFilms,
        allPresenters,
        filmPresenters,
        insights,
        allNewsletters,
        filmTags
      )
      adminView = new BackstageView(adminService)
    }
    ws.url("http://localhost:9000/healthcheck").get().map(x => {
      renderedView.setBlogHomepage(x.body)
      renderedView.lastUpdated
    })
  }

  val interval = 30

  def start(implicit db: Database, system: ActorSystem, ws: WSClient) = {
    system.scheduler.schedule(0.seconds, interval.seconds)(updateView)
  }

  def wildvisionView = renderedView
  def backstageView = adminView
  def backstageService = adminService
}
