package services

import play.api.db.Database
import anorm._

object InsightService {
  def record(insightEntity: String, insightType: String, entityId: Int, ip: String)(implicit db: Database): Option[Long] = {
      db.withConnection { implicit connection =>
        SQL("""
            INSERT INTO insights (insight_entity, insight_type, entity_id, visitor_ip)
            VALUES ({insightEntity}, {insightType}, {entityId}, {ip})
          """)
          .on("insightEntity" -> insightEntity, "insightType" -> insightType, "entityId" -> entityId, "ip" -> ip)
          .executeInsert()
      }
  }
}
