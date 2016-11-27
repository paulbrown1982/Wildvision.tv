package services

import models.Types.PresenterId
import models._

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

}
