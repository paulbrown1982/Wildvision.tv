package views.wildvision

import models.Types.UnsafeTag
import models._
import views._

import scala.collection.immutable.Seq.empty

class DynamicView(filmViews: Seq[FilmView] = empty,
                  presenterViews: Seq[PresenterView] = empty,
                  newsletterViews: Seq[NewsletterView] = empty) {

  implicit class NotFound(in: Int) {
    def found: Option[Int] = if (in != -1) Some(in) else None
  }

  private def finding(needle: String)(barn: Barn): Int = {
    if (barn.name.startsWith(s"$needle ")) { 1 } else {
      barn.haystack.indexOf(s" $needle ").found.fold {
        barn.haystack.indexOf(s" $needle").found.fold {
          barn.haystack.indexOf(s"$needle ").found.fold {
            barn.haystack.indexOf(needle) * 10000
          } { _ * 1000 }
        } { _ * 100 }
      } { _ * 10 }
    }
  }

  private val searchBase = html.wildvision.search(None)

  def cookies(optout: Boolean, dnt: Boolean) = html.wildvision.cookies(optout, dnt)

  def filmsWithTag(tagOpt: Option[String]) = {
    tagOpt.fold(searchBase) { unsafeTagString =>
      val unsafeTag = new UnsafeTag(unsafeTagString)
      val safeTag = unsafeTag.getSafeTag
      safeTag.fold(searchBase) { tag =>
        html.wildvision.filmsWithTag(filmViews.filter(_.tagSet.contains(tag)).sortBy(_.film.film_name), tag)
      }
    }
  }

  def search(q: Option[String]) = {
    q.fold(searchBase) { query =>
      val sanitised = if (query.trim.length > 2) Some(query.trim.toLowerCase) else None
      sanitised.fold(searchBase) { needle =>
        val foundFilmViews = filmViews.filter(_.haystack.contains(needle)).sortBy(finding(needle))
        val foundPresenterViews = presenterViews.filter(_.haystack.contains(needle)).sortBy(finding(needle))
        val foundNewsletterViews = newsletterViews.filter(_.haystack.contains(needle)).sortBy(finding(needle))
        html.wildvision.search(Some(SearchResults(query, foundFilmViews, foundPresenterViews, foundNewsletterViews)))
      }
    }
  }
}
