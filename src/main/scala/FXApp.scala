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

import gui._

object FXApp extends JFXApp {
  // app init
  me.demongoo.merchapp.db.boot() // db bootstrap

  val bp = new BorderPane {
    // left pane is menu
    left = AppHelper.buildLeftMenu(Seq(
      MenuConf.Section("БД", Seq(
        MenuConf.MenuItem("Продавцы", () => showPage(new pages.Index)),
        MenuConf.MenuItem("Товары", () => println("Products")),
        MenuConf.MenuItem("Карты", () => println("Cards"))
      )),
      MenuConf.Section("Аналитика")
    ))

    center = new VBox {
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
        new Button("Fuck You") {
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

  def showPage(page: Page): Unit = bp.center = page.build
}
