package views.backstage

import models.Presenter
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import play.twirl.api.Html
import services.BackstageService

class BackstageView(service: BackstageService) {
  def login: Html = views.html.backstage.login(None)
  def failedLogin: views.html.backstage.login.type = views.html.backstage.login
  def studioOf(presenter: Presenter)(implicit messages: Messages) = views.html.backstage.studio(presenter)
}

object BackstageView {
  val presenterForm: Form[Presenter] = Form(
    mapping(
      "live" -> char,
      "presenter_id" -> number,
      "name" -> text,
      "picture_url" -> text,
      "twitter_username" -> optional(text),
      "private_email_address" -> text,
      "bio" -> text,
      "p_lastupdated" -> jodaDate
    )(Presenter.apply)(Presenter.unapply)
  )
}