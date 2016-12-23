package views

import java.util.regex.Pattern

import com.github.nscala_time.time.OrderingImplicits.LocalDateOrdering
import models.Types.Tag
import models._
import org.joda.time.Duration._
import org.joda.time.LocalDate.now
import org.joda.time.{LocalDate, Period}
import org.joda.time.format.{DateTimeFormatterBuilder, PeriodFormat, PeriodFormatterBuilder}
import play.twirl.api.Html
import views.Formatters._

object Formatters {

  private val linkRegex = Pattern.compile("(http[s]?://[^\\s]+)")
  private val linkRegexReplace = "<a target='_blank' href='$1'>$1</a>"
  private val newlineRegex = Pattern.compile("\n")
  private val newlineRegexReplace = "<br/>"

  def toHtml(str: String): Html = {
    val trimmed = str.trim
    if (trimmed.isEmpty) {
      Html("")
    } else {
      Html(newlineRegex.matcher(linkRegex.matcher(trimmed).replaceAll(linkRegexReplace)).replaceAll(newlineRegexReplace))
    }
  }

  val monthYearFormatter = new DateTimeFormatterBuilder().appendDayOfMonth(1).appendLiteral(' ').appendMonthOfYearText.appendLiteral(' ').appendYear(4, 4)
  val publishedDateFormatter = PeriodFormat.getDefault
  val durationFormatter = new PeriodFormatterBuilder().printZeroAlways.appendMinutes.appendSeparator(":").minimumPrintedDigits(2).printZeroAlways.appendSeconds.toFormatter

  def formatPublishedDate(date: LocalDate): String = {
    publishedDateFormatter.print(new Period(date, now)).split(',').head.split(" and ").head.trim + " ago"
  }
}

object ModelEnhancements {
  implicit class FilmWithImageUrl(film: Film) {
    def imageUrl: String = s"https://res.cloudinary.com/wildvision-tv/image/fetch/c_fill,dpr_1.0,fl_progressive,g_center,h_169,w_300/https://wildvision.tv/images/films/${film.film_image}"
  }
  implicit class PresenterWithImageUrl(presenter: Presenter) {
    def imageUrl: String = s"https://res.cloudinary.com/wildvision-tv/image/fetch/c_fill,dpr_1.0,fl_progressive,g_faces,h_300,w_300/https://wildvision.tv/images/presenters/${presenter.picture_url}"
  }
}


trait Barn {
  def haystack: String
  def name: String
}

case class FilmView(film: Film, presenters: Seq[Presenter], viewings: Int, impressions: Int, tags: Seq[FilmTag]) extends Barn {
  import ModelEnhancements._

  val entityId: String = film.film_id.toString
  val duration: String = durationFormatter.print(standardSeconds(film.duration).toPeriod)
  val published: String = formatPublishedDate(film.film_published_date)
  val filmEmbedUrl = film.film_host match {
    case s if s.equals("YOUTUBE") => s"https://www.youtube.com/embed/${film.film_host_id}?rel=0&autohide=1&showinfo=0"
    case s if s.equals("VIMEO") => s"https://player.vimeo.com/video/${film.film_host_id}?title=0&byline=0&portrait=0"
    case _ => s"https://wildvision.tv/films/embed/${film.film_host_id}"
  }
  val asTemplateMapEntry: (Slug, Html) = film.slug -> html.wildvision.film(this)
  val haystack = film.toString.toLowerCase
  val name = film.film_name.toLowerCase
  val tagSet = tags.map(_.tag).toSet[Tag]
  lazy val imageUrl: String = film.imageUrl
}

case class PresenterView(presenter: Presenter, films: Seq[Film]) extends Barn {
  import ModelEnhancements._

  val entityId: String = presenter.presenter_id.toString
  val asTemplateMapEntry: (Slug, Html)  = presenter.slug -> html.wildvision.presenter(this)
  def withFilmsReversed: PresenterView = this.copy(films = this.films.reverse)
  def debut = this.films.sortBy(_.film_published_date).head.film_published_date
  def filmsPublishedThisYear = this.films.filter(_.film_published_date.isBefore(now.minusYears(1)))
  val haystack = presenter.toString.toLowerCase
  val name = presenter.name.toLowerCase
  lazy val imageUrl: String = presenter.imageUrl
}

case class EditorsChoiceFilmView(filmView: FilmView, description: String)

case class NewsletterView(newsletter: Newsletter, otherNewsletters: Seq[Newsletter]) extends Barn {
  val entityId: String = newsletter.newsletter_id.toString
  val published = monthYearFormatter.toFormatter.print(newsletter.date_published)
  val asTemplateMapEntry: (Slug, Html)  = newsletter.slug -> html.wildvision.newsletter(this)
  val haystack = this.toString.toLowerCase
  val name = newsletter.title.toLowerCase
}

case class TagCount(tag: Tag, size: Int)
