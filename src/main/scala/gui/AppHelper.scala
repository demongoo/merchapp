package me.demongoo.merchapp.gui

import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.Includes._

object MenuConf {
  sealed trait Position
  case class MenuItem(title: String, action: () => Unit) extends Position
  case class Section(title: String, items: Seq[MenuItem] = Seq()) extends Position
}

object AppHelper {
  /**
   * Builds panel with left menu
   * @param items List of menu items
   * @return Pane with menu
   */
  def buildLeftMenu(items: Seq[MenuConf.Section]): Pane = new VBox {
    styleClass = Seq("left-menu")

    children = items flatMap { sect =>
      Seq(new Label(sect.title)) ++ sect.items.map {
        case MenuConf.MenuItem(title, action) => new Hyperlink {
          text = title
          onAction = handle { action() }
        }
      }
    }
  }
}