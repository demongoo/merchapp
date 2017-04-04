/**
 * Schema definition for database
 */

package me.demongoo.merchapp.db

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.{ Schema, Table }
import org.squeryl.annotations.Column
import java.util.Date
import java.sql.Timestamp
import org.squeryl.KeyedEntity

class Merchant(
  val id: Long = 0,
  @Column("parent_id") var parentId: Option[Long] = None,
  var level: Int = 1,
  @Column(length = 50) var name: String,
  @Column(length = 50) var company: Option[String] = None,
  @Column(length = 50) var position: Option[String] = None,
  @Column(length = 25) var phone: Option[String] = None,
  @Column(length = 50) var email: Option[String] = None,
  var social: Option[String] = None,
  var card: Option[String] = None,
  var active: Boolean = true,
  val ts: Timestamp = new Timestamp(System.currentTimeMillis)
) extends KeyedEntity[Long]


object MerchantDb extends Schema {
  val merchants: Table[Merchant] = table[Merchant]("merchants")
}