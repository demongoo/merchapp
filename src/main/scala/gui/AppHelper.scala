package me.demongoo.merchapp.gui

import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.Includes._

object MenuConf {
  sealed trait Position
  case class MenuItem(title: String, action: () => Unit) extends Position
  case class Header(title: String) extends Position
}

object AppHelper {
  /**
   * Builds panel with left menu
   * @param items List of menu items
   * @return Pane with menu
   */
  def buildLeftMenu(items: Seq[MenuConf.Position]): Pane = new VBox {
    styleClass = Seq("left-menu")

    children = items map {
      case MenuConf.Header(title) => new Label(title)

      case MenuConf.MenuItem(title, action) => new Hyperlink {
        text = title
        onAction = handle { action() }
      }
    }
  }
}