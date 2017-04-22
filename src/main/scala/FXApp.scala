/**
 * GUI Entry point
 */

package me.demongoo.merchapp

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.{Insets, NodeOrientation, Pos}
import scalafx.scene.Scene
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout._
import scalafx.scene.paint._
import scalafx.scene.control._
import scalafx.collections.ObservableBuffer
import scalafx.event._
import scalafx.scene.input._

object FXApp extends JFXApp {
  // app init
  me.demongoo.merchapp.db.boot() // db bootstrap

  def sqpla(): Unit = {
    import org.squeryl.PrimitiveTypeMode._
    import me.demongoo.merchapp.db._

    def println(v: Any): Unit = {
      val b = new StringBuilder
      b.append(v)
      ta.text() += b.toString() + "\n"
    }
    
    transaction {
      try
        MerchantDb.merchants.head
      catch {
        case _: RuntimeException => MerchantDb.printDdl; MerchantDb.create
      }
    }

    // sample data
    transaction {
      // merchant 1
      val m1 = MerchantDb.merchants.insert({
        val m = new Merchant(name = "Dmitry", company = Some("FMW"), email = Some("dmitry.revenko@gmail.com"))
        m.setSocials(Seq(
          SocialRef("facebook", "2323232"),
          SocialRef("skype", "dmitry.revenko")
        ))
        m
      })

      // merchants 2 and 3
      val m2 = MerchantDb.merchants.insert({
        val m = new Merchant(name = "Cot", company = Some("Beegle"), email = Some("kort@gmail.com"))
        m.setParent(m1)
        m
      })

      val m3 = MerchantDb.merchants.insert({
        val m = new Merchant(name = "Pet", company = Some("Froogle"), email = Some("chort@gmail.com"))
        m.setParent(m1)
        m
      })

      // merchant 4
      val m4 = MerchantDb.merchants.insert({
        val m = new Merchant(name = "Level 3 merch", company = Some("Beegle"), email = Some("ukuk@gmail.com"))
        m.setParent(m3)
        m
      })

      println(MerchantDb.merchants.take(5).toList)

      val m = MerchantDb.merchants.lookup(4)
      println(m.get.parent)
      println(m.get.children.toList)

      val n = MerchantDb.merchants.lookup(1)
      println(n.get.parent)
      println(n.get.children.toList)
    }
  }

  val ta = new TextArea {
    id = "ta"
    alignmentInParent = Pos.Center
    vgrow = Priority.Always
    hgrow = Priority.Always
  }

  val bp = new BorderPane {
    style = "-fx-base: yellow"

    center = new VBox {
      style = "-fx-background: blue"
      spacing = 10
      fillWidth = true
      children = Seq(
        new MenuBar {
          menus = Seq(
            new Menu("One") {
              items = List("What", "The", "Fuck") map (new MenuItem(_))
            },
            new Menu("Two") {
              items = List("Was", "ist", "das") map (new MenuItem(_))
            },
            new Menu("Exit") {
              accelerator = KeyCombination.keyCombination("Ctrl+C")
              onShowing = handle {
                System.exit(0)
              }
            }
          )
        },
        ta,
        new Button("Fuck You") {
          onMouseClicked = handle {
            sqpla()
          }
          onMouseEntered = handle {
            effect = new DropShadow()
          }
          onMouseExited = handle {
            effect = null
          }
        },
        new HBox {
          spacing = 5
          alignment = Pos.CenterLeft
          children = Seq(
            new Label("Place"),
            new ChoiceBox[String] {
              items = ObservableBuffer("Earth", "Sky", "Paradise")
            }
          )
        },
        new TextField {
          promptText = "Eto i pravda nechto"
          onMouseClicked = (evt: Event) => text = evt.toString
        }
      )
    }
  }

  stage = new PrimaryStage {
    title = "Merchant App 1.0"
    maximized = true
    
    scene = new Scene {
      fill = Color.rgb(240, 240, 240)
      maximized = true
      stylesheets.add(getClass.getResource("/main.css").toExternalForm)

      content = bp
    }
  }

  bp.prefWidth <== stage.width
  bp.prefHeight <== stage.height
}
