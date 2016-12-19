package services

import anorm.{NamedParameter, _}
import models.Types.{FilmId, PresenterId}
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
    val filmIdsByThisPresenter = filmPresenters.filter(_.presenter_id == presenterId).map(_.film_id).toSet
    allFilms.filter(film => filmIdsByThisPresenter.contains(film.film_id)).sortBy(_.f_lastupdated).reverse
  }

  def presenterOwnsFilm(presenterId: PresenterId, filmId: FilmId): Boolean = {
    filmPresenters.filter(_.presenter_id == presenterId).exists(_.film_id == filmId)
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

  def updatePresenterFilm(film: Film)(implicit db: Database): Unit = {
    db.withConnection { implicit connection =>
      SQL(
        """
          UPDATE `films` SET
            `live` = {live},
            `film_name` = {film_name},
            `film_image` = {film_image},
            `film_host` = {film_host},
            `film_host_id` = {film_host_id},
            `film_published_date` = {film_published_date},
            `film_description` = {film_description},
            `duration` = {duration},
            `latitude` = {latitude},
            `longitude` = {longitude},
            `f_lastupdated` = {f_lastupdated}
          WHERE `film_id` = {film_id}
        """)
        .on(
          NamedParameter("film_id", film.film_id),
          NamedParameter("live", film.live),
          NamedParameter("film_name", film.film_name),
          NamedParameter("film_image", film.film_image),
          NamedParameter("film_host", film.film_host),
          NamedParameter("film_host_id", film.film_host_id),
          NamedParameter("film_published_date", film.film_published_date.toString("yy-MM-dd")),
          NamedParameter("film_description", film.film_description),
          NamedParameter("duration", film.duration),
          NamedParameter("latitude", film.latitude),
          NamedParameter("longitude", film.longitude),
          NamedParameter("f_lastupdated", film.f_lastupdated.toString("yy-MM-dd HH:mm:ss"))
        )
        .executeInsert()
    }
  }
}
