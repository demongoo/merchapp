/**
 * Schema definition for database
 */

package me.demongoo.merchapp

import java.util.Date
import java.sql.Timestamp

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.H2Adapter
import org.squeryl._
import org.squeryl.dsl._
import org.squeryl.annotations.{Column, ColumnBase}

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
    @Column(length = 25) var card: Option[String] = None,
    var active: Boolean = true,
    val ts: Timestamp = new Timestamp(System.currentTimeMillis)
  ) extends KeyedEntity[Int] {
    def this() = this(0, Some(0), 0, "", Some(""), Some(""), Some(""), Some(""), Some(""), Some(""), true, new Timestamp(System.currentTimeMillis()))


    // social network ids
    def getSocials: Seq[SocialRef] = social.map(_.split("\n").toSeq.map(_.trim).map(_.split(":", 2)).collect {
      case Array(nw, ident) => SocialRef(nw, ident)
    }).getOrElse(Seq.empty)

    def setSocials(refs: Seq[SocialRef]): Unit = if (refs.nonEmpty)
      Some(refs.map(r => r.network + ":" + r.id).mkString("\n"))
    else
      None


    /**
     * Parent merchant
     *
     * @return Parent merchant if any
     */
    def parent: Option[Merchant] = MerchantDb.parent2merchants.right(this).take(1).headOption


    /**
     * Set the parent merchant
     *
     * @param m Parent merchant
     */
    def setParent(m: Merchant): Unit = {
      parentId = Some(m.id)
      level = m.id + 1
    }


    /**
     * Child merchants of this merchant
     *
     * @return Child merchants
     */
    lazy val children: OneToMany[Merchant] = MerchantDb.parent2merchants.left(this)
  }

  // Merchant discount
  case class MerchantDiscount(id: Int = 0, @Column("merchant_id") merchantId: Int, value: BigDecimal, start: Timestamp, end: Option[Timestamp] = None) extends KeyedEntity[Int] {
    def this() = this(0, 0, BigDecimal(0), new Timestamp(System.currentTimeMillis()), Some(new Timestamp(System.currentTimeMillis())))
  }

  // Merchant reward
  case class MerchantReward(id: Int = 0, @Column("merchant_id") merchantId: Int, value: BigDecimal, start: Timestamp, end: Option[Timestamp] = None) extends KeyedEntity[Int] {
    def this() = this(0, 0, BigDecimal(0), new Timestamp(System.currentTimeMillis()), Some(new Timestamp(System.currentTimeMillis())))
  }

  // Available card storage
  case class IssuedCard(id: Int = 0, @Column(length = 25) number: String, given: Boolean = false) extends KeyedEntity[Int]

  // cards given out to some merchant to supply to next level merchants
  case class MerchantCard(id: Int = 0, @Column("merchant_id") merchantId: Int, @Column(length = 25) number: Int) extends KeyedEntity[Int]

  // Product category
  case class ProductCategory(id: Int = 0, @Column(length = 50) title: String) extends KeyedEntity[Int]

  // Product
  class Product(
    val id: Int = 0,
    @Column("category_id") var categoryId: Option[Int] = None,
    @Column(length = 50) var name: String,
    var info: Option[String] = None,
    @Column(name = "serial_number", length = 25) var serialNumber: Option[String] = None,
    var cost: Option[BigDecimal] = None,
    var active: Boolean = true,
    val ts: Timestamp = new Timestamp(System.currentTimeMillis)
  ) extends KeyedEntity[Int] {
    def this() = this(0, Some(0), "", Some(""), Some(""), Some(BigDecimal(0)), true, new Timestamp(System.currentTimeMillis()))
  }

  // Product price
  case class ProductPrice(id: Int = 0, @Column("product_id") productId: Int, value: BigDecimal, start: Timestamp, end: Option[Timestamp] = None) extends KeyedEntity[Int] {
    def this() = this(0, 0, BigDecimal(0), new Timestamp(System.currentTimeMillis()), Some(new Timestamp(System.currentTimeMillis())))
  }

  // Order
  case class Order(id: Int = 0, @Column("merchant_id") merchantId: Int, ts: Timestamp = new Timestamp(System.currentTimeMillis)) extends KeyedEntity[Int]

  // Order Item
  case class OrderItem(id: Int = 0, @Column("order_id") orderId: Int, @Column("product_id") productId: Int, quantity: Int = 1) extends KeyedEntity[Int]


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

    // merchant discounts
    val merchantDiscounts: Table[MerchantDiscount] = table[MerchantDiscount]("merchant_discounts")
    on(merchantDiscounts)(md => declare(
      md.id is autoIncremented,
      md.value is dbType("decimal(4,2)"),
      md.start is indexed,
      md.end is indexed
    ))

    // merchant rewards
    val merchantRewards: Table[MerchantReward] = table[MerchantReward]("merchant_rewards")
    on(merchantRewards)(mr => declare(
      mr.id is autoIncremented,
      mr.value is dbType("decimal(4,2)"),
      mr.start is indexed,
      mr.end is indexed
    ))

    // cards issued
    val cardsIssued: Table[IssuedCard] = table[IssuedCard]("cards_issued")
    on(cardsIssued)(ci => declare(
      ci.id is autoIncremented,
      ci.number is unique,
      ci.given defaultsTo false
    ))

    // merchant cards (cards posessed by some merchant to supply to his next level merchants)
    val merchantCards: Table[MerchantCard] = table[MerchantCard]("merchant_cards")
    on(merchantCards)(mc => declare(
      mc.id is autoIncremented
    ))

    // product categories
    val productCategories: Table[ProductCategory] = table[ProductCategory]("product_categories")
    on(productCategories)(pc => declare(
      pc.id is autoIncremented
    ))

    // products
    val products: Table[Product] = table[Product]("products")
    on(products)(p => declare(
      p.id is autoIncremented,
      p.info is dbType("text"),
      p.active defaultsTo true,
      p.cost is dbType("decimal(15,2)")
    ))

    // product prices
    val productPrices: Table[ProductPrice] = table[ProductPrice]("product_prices")
    on(productPrices)(pp => declare(
      pp.id is autoIncremented,
      pp.value is dbType("decimal(15,2)"),
      pp.start is indexed,
      pp.end is indexed
    ))

    // orders
    val orders: Table[Order] = table[Order]("orders")
    on(orders)(o => declare(
      o.id is autoIncremented
    ))

    // order items
    val orderItems: Table[OrderItem] = table[OrderItem]("order_items")
    on(orderItems)(oi => declare(
      oi.id is autoIncremented,
      oi.quantity defaultsTo 1
    ))

    // relationships
    val parent2merchants = oneToManyRelation(merchants, merchants).via((p, m) => p.id === m.parentId)
    val merchant2discounts = oneToManyRelation(merchants, merchantDiscounts).via((m, md) => m.id === md.merchantId)
    val merchant2rewards = oneToManyRelation(merchants, merchantRewards).via((m, mr) => m.id === mr.merchantId)
    val merchant2cards = oneToManyRelation(merchants, merchantCards).via((m, mc) => m.id === mc.merchantId)
    val category2products = oneToManyRelation(productCategories, products).via((pc, p) => pc.id === p.categoryId)
    val product2prices = oneToManyRelation(products, productPrices).via((p, pp) => p.id === pp.productId)
    val merchant2orders = oneToManyRelation(merchants, orders).via((m, o) => m.id === o.merchantId)
    val order2items = oneToManyRelation(orders, orderItems).via((o, oi) => o.id === oi.orderId)
    val product2orderItems = oneToManyRelation(products, orderItems).via((p, oi) => p.id === oi.productId)
  }
}