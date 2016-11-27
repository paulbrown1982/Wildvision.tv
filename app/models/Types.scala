package models

object Types {
  type FilmId = Int
  type PresenterId = Int
  type NewsletterId = Int
  type Count = Int
  class UnsafeTag(val name: String) extends AnyVal {
    def getSafeTag: Option[Tag] = {
      val sanitised = name.filter(_.isLetterOrDigit)
      if (sanitised.length > 2) Some(new Tag(sanitised)) else None
    }
  }
  type Tag = String
}