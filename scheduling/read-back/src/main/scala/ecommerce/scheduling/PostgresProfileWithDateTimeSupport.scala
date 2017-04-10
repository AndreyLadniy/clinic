package ecommerce.scheduling

import com.github.tminglei.slickpg.{ExPostgresProfile, PgDate2Support}

trait PostgresProfileWithDateTimeSupport extends ExPostgresProfile with PgDate2Support {

  override val api: API = new API {}

  trait API extends super.API
    with DateTimeImplicits

}

object PostgresProfileWithDateTimeSupport extends PostgresProfileWithDateTimeSupport

//trait MyPostgresProfile extends ExPostgresProfile
//  with PgArraySupport
//  with PgDateSupport
//  with PgDate2Support
//  with PgJsonSupport
//  with PgNetSupport
//  with PgLTreeSupport
//  with PgRangeSupport
//  with PgHStoreSupport
//  with PgSearchSupport {
//
//  override val pgjson = "jsonb"
//  ///
//  override val api: API = new API {}
//
//  trait API extends super.API with ArrayImplicits
//    with SimpleDateTimeImplicits
//    with DateTimeImplicits
//    with SimpleJsonImplicits
//    with NetImplicits
//    with LTreeImplicits
//    with RangeImplicits
//    with HStoreImplicits
//    with SearchImplicits
//    with SearchAssistants
//  ///
//  val plainAPI = new API with ByteaPlainImplicits
//    with SimpleArrayPlainImplicits
//    with Date2DateTimePlainImplicits
//    with SimpleJsonPlainImplicits
//    with SimpleNetPlainImplicits
//    with SimpleLTreePlainImplicits
//    with SimpleRangePlainImplicits
//    with SimpleHStorePlainImplicits
//    with SimpleSearchPlainImplicits {}
//}
