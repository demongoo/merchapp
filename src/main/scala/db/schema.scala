/**
 * Schema definition for database
 */

package me.demongoo.merchapp

import java.util.Date
import java.sql.Timestamp

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.H2Adapter
import org.squeryl._
import org.squeryl.annotations.Column

package object `db` {
  /**
   * Database bootstrap
   */
  def boot(): Unit = {
    Class.forName("org.h2.Driver")
    SessionFactory.concreteFactory = Some(() => Session.create(
      java.sql.DriverManager.getConnection("jdbc:h2:mem:dbname;DB_CLOSE_DELAY=-1", "sa", ""),
      new H2Adapter
    ))
  }


  /**
   * Holder for some social network and messenger reference
   *
   * @param network Network code
   * @param id Network ID
   */
  case class SocialRef(network: String, id: String)
  object SocialRef {
    case class SocialNetworkMeta(code: String, name: String, icon: Option[String] = None)
    val Networks: Seq[SocialNetworkMeta] = Seq(
      SocialNetworkMeta("skype", "Skype"),
      SocialNetworkMeta("facebook", "Facebook"),
      SocialNetworkMeta("vkontakte", "ВКонтакте")
    )
  }


  // Merchant
  class Merchant(
    val id: Int = 0,
    @Column("parent_id") var parentId: Option[Int] = None,
    var level: Int = 1,
    @Column(length = 50) var name: String,
    @Column(length = 50) var company: Option[String] = None,
    @Column(length = 50) var position: Option[String] = None,
    @Column(length = 25) var phone: Option[String] = None,
    @Column(length = 50) var email: Option[String] = None,
    var social: Option[String] = None,
    @Column(length = 50) var card: Option[String] = None,
    var active: Boolean = true,
    val ts: Timestamp = new Timestamp(System.currentTimeMillis)
  ) extends KeyedEntity[Int] {
    def this() = this(0, Some(0), 0, "", Some(""), Some(""), Some(""), Some(""), Some(""), Some(""), true, new Timestamp(System.currentTimeMillis()))

    def getSocials: Seq[SocialRef] = social.map(_.split("\n").toSeq.map(_.trim).map(_.split(":", 2)).collect {
      case Array(nw, ident) => SocialRef(nw, ident)
    }).getOrElse(Seq.empty)

    def setSocials(refs: Seq[SocialRef]): Unit = if (refs.nonEmpty)
      Some(refs.map(r => r.network + ":" + r.id).mkString("\n"))
    else
      None
  }

  // Available card storage
  case class IssuedCard(id: Int = 0, number: String, given: Boolean = false) extends KeyedEntity[Int]


  object MerchantDb extends Schema {
    // merchants
    val merchants: Table[Merchant] = table[Merchant]("merchants")
    on(merchants)(m => declare(
      m.id is autoIncremented,
      m.level defaultsTo 1,
      m.social is dbType("text"),
      m.card is unique,
      m.active defaultsTo true
    ))

    // cards issued
    val cardsIssued: Table[IssuedCard] = table[IssuedCard]("cards_issued")
    on(cardsIssued)(ci => declare(
      ci.id is autoIncremented,
      ci.number is unique,
      ci.given defaultsTo false
    ))

    // relationships
    val parent2merchants = oneToManyRelation(merchants, merchants).via((p, m) => p.id === m.parentId)
  }
}