package models

import models.Types.{FilmId, NewsletterId, PresenterId, Tag}
import org.joda.time.{DateTime, LocalDate}
import views.{FilmView, NewsletterView, PresenterView}

case class Slug(base: String) {
  override def toString = base
}

object Slug {
  def fromSlugString(str: String): Slug = {
    Slug(str)
  }
  def fromString(str: String): Slug = {
    Slug(str.toLowerCase.replace(' ', '-'))
  }
}

sealed trait SitemapSection
case object SitemapFilms extends SitemapSection
case object SitemapPresenters extends SitemapSection
case object SitemapNewsletters extends SitemapSection

case class Film(live: Char,
                film_id: FilmId,
                film_name: String,
                film_image: String,
                film_host: String,
                film_host_id: String,
                film_published_date: LocalDate,
                latitude: Option[BigDecimal],
                longitude: Option[BigDecimal],
                duration: Int,
                film_description: String,
                f_lastupdated: DateTime) {
  val slug = Slug.fromString(film_name)
  val path = s"/film/${slug.toString}"
}

case class Presenter(live: Char,
                     presenter_id: PresenterId,
                     name: String,
                     picture_url: String,
                     twitter_username: Option[String],
                     private_email_address: String,
                     bio: String,
                     p_lastupdated: DateTime) {
  val slug = Slug.fromString(name)
  val path = s"/presenter/${slug.toString}"
  val anchor = <a href="{path}">{name}</a>.toString.replace("{path}", path)
}

case class Config(item: String, value: String) {
  val asMapEntry = item -> value
}

case class FilmPresenter(film_id: FilmId, presenter_id: PresenterId)

case class Insight(insight_entity: String, insight_type: String, entity_id: Int, at_this_time: DateTime)

case class Newsletter(newsletter_id: NewsletterId, date_published: LocalDate, title: String, url: String, image_src: String) {
  val slug = Slug.fromString(title)
  val path = s"/newsletter/${slug.toString}"
}

case class FilmTag(film_id: FilmId, tag: Tag, ft_lastupdated: DateTime)

case class SearchResults(query: String, films: Seq[FilmView], presenters: Seq[PresenterView], newsletters: Seq[NewsletterView]) {

}