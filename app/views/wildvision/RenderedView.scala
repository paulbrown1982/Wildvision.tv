package views.wildvision

import com.github.nscala_time.time.OrderingImplicits.LocalDateOrdering
import models._
import org.joda.time.DateTime
import org.joda.time.DateTime.now
import play.twirl.api.Html
import views._

import scala.collection.immutable.Seq.empty

class RenderedView(config: Seq[Config] = empty,
                   activeFilms: Seq[Film] = empty,
                   activePresenters: Seq[Presenter] = empty,
                   filmPresenters: Seq[FilmPresenter] = empty,
                   insights: Seq[Insight] = empty,
                   allNewsletters: Seq[Newsletter] = empty,
                   filmTags: Seq[FilmTag] = empty) {

  private val activeFilmTags = filmTags.filter(ft => activeFilms.exists(_.film_id == ft.film_id))
  private val filmTagsByFilmId = activeFilmTags.groupBy(_.film_id)
  private val tagMap = activeFilmTags.groupBy(_.tag).mapValues(_.size)
  private val filmTagCounts = tagMap.map(x => TagCount(x._1, x._2)).toSeq

  private val impressionsPerFilmId = insights
    .filter(i => i.insight_entity == "FILM" && i.insight_type == "IMPRESSED" && i.at_this_time.isAfter(now.minusDays(30)))
    .groupBy(_.entity_id)
    .mapValues(_.size)
  private val viewsPerFilmId = insights
    .filter(i => i.insight_entity == "FILM" && i.insight_type == "VIEWED")
    .groupBy(_.entity_id)
    .mapValues(_.size)

  private val filmViews = {
    for {
      film <- activeFilms
    } yield {
      val numberOfViews = viewsPerFilmId.getOrElse(film.film_id, 0)
      val numberOfImpressions = impressionsPerFilmId.getOrElse(film.film_id, 0)
      val presenters = for {
        filmPresenter <- filmPresenters.filter(_.film_id == film.film_id)
        presenter <- activePresenters.filter(_.presenter_id == filmPresenter.presenter_id)
      } yield {
        presenter
      }
      FilmView(film, presenters.sortBy(_.name), numberOfViews, numberOfImpressions, filmTagsByFilmId.getOrElse(film.film_id, empty))
    }
  }.sortBy(_.film.film_published_date).reverse

  private val presenterViews = {
    for {
      presenter <- activePresenters
    } yield {
      val films = for {
        filmPresenter <- filmPresenters.filter(_.presenter_id == presenter.presenter_id)
        film <- activeFilms.filter(_.film_id == filmPresenter.film_id)
      } yield {
        film
      }
      PresenterView(presenter, films.sortBy(_.film_published_date).reverse)
    }
  }.sortBy(_.films.head.film_published_date).reverse

  private val newsletterViews = allNewsletters.map(n => {
    val partitioned = allNewsletters.partition(_ == n)
    NewsletterView(partitioned._1.head, partitioned._2.sortBy(_.date_published).reverse)
  }).sortBy(_.newsletter.date_published).reverse

  private val top10ShortestFilmViews = filmViews.sortBy(_.film.duration).take(10)
  private val top10LongestFilmViews = filmViews.sortBy(_.film.duration).reverse.take(10)
  private val top10MostPopularFilmViews = filmViews.sortBy(_.viewings).reverse.take(10)
  private val top10MostRecentFilmViews = filmViews.sortBy(_.film.film_published_date).reverse.take(10)
  private val top10TrendingFilmViews = filmViews.sortBy(_.impressions).reverse.take(10)
  private val top10Tags = filmTagCounts.sortBy(_.size).reverse.take(10).sortBy(_.tag)
  private val presenterHtmlMap = presenterViews.map(_.asTemplateMapEntry).toMap
  private val filmHtmlMap = filmViews.map(_.asTemplateMapEntry).toMap
  private val newsletterHtmlMap = newsletterViews.map(_.asTemplateMapEntry).toMap
  private val configMap = config.map(_.asMapEntry).toMap

  private val editorsChoiceFilmView = {
    for {
      filmId <- configMap.get("HOMEPAGE_FILM_ID")
      description <- configMap.get("HOMEPAGE_FILM_DESC")
      film <- filmViews.find(_.film.film_id == filmId.toInt)
    } yield {
      EditorsChoiceFilmView(film, description)
    }
  }

  def dynamicView: DynamicView = new DynamicView(filmViews, presenterViews, newsletterViews)

  val about: Html = html.wildvision.about()
  val films: Html = html.wildvision.films(filmViews, "all")
  val filmsTagged: Html = html.wildvision.films(empty, "tagged", filmTagCounts, top10Tags)
  val presenters: Html = html.wildvision.presenters(presenterViews, "all")
  val editorsChoiceFilm: Option[Html] = editorsChoiceFilmView.map(html.wildvision.editorschoice.apply)
  val top10MostPopularFilms: Html = html.wildvision.films(top10MostPopularFilmViews, "popular")
  val top10MostRecentFilms: Html = html.wildvision.films(top10MostRecentFilmViews, "latest")
  val top10TrendingFilms: Html = html.wildvision.films(top10TrendingFilmViews, "trending")
  val top10LongestFilms: Html = html.wildvision.films(top10LongestFilmViews, "longest")
  val top10ShortestFilms: Html = html.wildvision.films(top10ShortestFilmViews, "shortest")
  val subscribe: Html = html.wildvision.subscribe(newsletterViews)
  val subscribeThankYou: Html = html.wildvision.subscribeThankYou()

  def getFilm(slug: Slug): Option[Html] = filmHtmlMap.get(slug)
  def getPresenter(slug: Slug): Option[Html] = presenterHtmlMap.get(slug)
  def getNewsletter(slug: Slug): Option[Html] = newsletterHtmlMap.get(slug)

  val sitemap: Html = html.wildvision.sitemap(
    activeFilms.sortBy(_.film_name),
    activePresenters.sortBy(_.name),
    allNewsletters.sortBy(_.date_published).reverse,
    tagMap.keys.toSeq.sorted
  )

  val privacy: Html = html.wildvision.privacy()

  val lastUpdated: Option[DateTime] = if (filmViews.isEmpty) None else Some(now)

  val error404: Html = html.wildvision.error404()

  var blogHomepage: Html = html.wildvision.blog("Loading...")

  def setBlogHomepage(response: String): Unit = {
    blogHomepage = html.wildvision.blog(response)
  }
}
