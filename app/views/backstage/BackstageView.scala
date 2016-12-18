package views.backstage

import models.{Film, Presenter}
import play.api.data.Forms.{number, _}
import play.api.data._
import play.api.i18n.Messages
import play.twirl.api.Html
import services.BackstageService

class BackstageView(service: BackstageService) {
  def login: Html = views.html.backstage.login(None)
  def failedLogin: views.html.backstage.login.type = views.html.backstage.login
  def studioOf(presenter: Presenter)(implicit messages: Messages) = {
    val presenterFilms = service.findFilmsByPresenterId(presenter.presenter_id)
    views.html.backstage.studio(presenter, presenterFilms)
  }
}

object BackstageView {
  val lastUpdatedDateTimePattern = "yyyy-MM-dd'T'HH:mm:ssZZ"
  val localDatePattern = "yyyy-MM-dd"
  val presenterForm: Form[Presenter] = Form(
    mapping(
      "live" -> char,
      "presenter_id" -> number,
      "name" -> text,
      "picture_url" -> text,
      "twitter_username" -> optional(text),
      "private_email_address" -> email,
      "bio" -> text,
      "p_lastupdated" -> jodaDate(lastUpdatedDateTimePattern)
    )(Presenter.apply)(Presenter.unapply)
  )
  val filmForm: Form[Film] = Form(
    mapping(
      "live" -> char,
      "film_id" -> number,
      "film_name" -> text,
      "film_image" -> text,
      "film_host" -> text,
      "film_host_id" -> text,
      "film_published_date" -> jodaLocalDate(localDatePattern),
      "latitude" -> optional(bigDecimal),
      "longitude" -> optional(bigDecimal),
      "duration" -> number,
      "film_description" -> text,
      "f_lastupdated" -> jodaDate(lastUpdatedDateTimePattern)
    )(Film.apply)(Film.unapply)
  )
  def inputHidden(field: Field): Html = Html.apply(
      "<input id='${field.id}' type='hidden' name='${field.name}' value='${field.value}'/>"
  )
}