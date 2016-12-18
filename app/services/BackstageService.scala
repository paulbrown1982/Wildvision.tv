package services

import anorm.{NamedParameter, _}
import models.Types.PresenterId
import models._
import play.api.db.Database
import com.github.nscala_time.time.OrderingImplicits.DateTimeOrdering
import scala.collection.immutable.Seq._

class BackstageService(config: Seq[Config] = empty,
                       allFilms: Seq[Film] = empty,
                       allPresenters: Seq[Presenter] = empty,
                       filmPresenters: Seq[FilmPresenter] = empty,
                       insights: Seq[Insight] = empty,
                       allNewsletters: Seq[Newsletter] = empty,
                       filmTags: Seq[FilmTag] = empty) {

  def findPresenterByEmail(emailAddress: String): Option[Presenter] =
    allPresenters.find(_.private_email_address.equalsIgnoreCase(emailAddress))

  def findPresenterById(id: PresenterId): Option[Presenter] =
    allPresenters.find(_.presenter_id == id)

  def findFilmsByPresenterId(presenterId: PresenterId): Seq[Film] = {
    val filmIdsByThisPresenter = filmPresenters.filter(_.presenter_id == presenterId).map(_.film_id)
    allFilms.filter(film => filmIdsByThisPresenter.contains(film.film_id)).sortBy(_.f_lastupdated).reverse
  }

  def updatePresenter(presenter: Presenter)(implicit db: Database): Unit = {
    db.withConnection { implicit connection =>
      SQL(
        """
          UPDATE `presenters` SET
            `live` = {live},
            `name` = {name},
            `picture_url` = {picture_url},
            `twitter_username` = {twitter_username},
            `private_email_address` = {private_email_address},
            `bio` = {bio},
            `p_lastupdated` = {p_lastupdated}
          WHERE `presenter_id` = {presenter_id}
        """)
        .on(
          NamedParameter("presenter_id", presenter.presenter_id),
          NamedParameter("live", presenter.live),
          NamedParameter("name", presenter.name),
          NamedParameter("picture_url", presenter.picture_url),
          NamedParameter("twitter_username", presenter.twitter_username),
          NamedParameter("private_email_address", presenter.private_email_address),
          NamedParameter("bio", presenter.bio),
          NamedParameter("p_lastupdated", presenter.p_lastupdated.toString("yy-MM-dd HH:mm:ss"))
        )
        .executeInsert()
    }
  }

}
