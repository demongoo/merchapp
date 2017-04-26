/**
 * Base page class, which generates layout for further actions
 */

package me.demongoo.merchapp.gui

import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.Includes._

abstract class Page {
  /** Page title */
  val title: String = "No title"

  /** Buttons on the top line */
  val buttons: Seq[Page.Button] = Seq.empty

  /** Inner content of active region */
  val content: scalafx.scene.Node = new Pane()

  def build: scalafx.scene.Node = new BorderPane {
    styleClass = Seq("right-pane")

    // top holds the caption and the buttons
    top = new AnchorPane {
      styleClass = Seq("right-pane-top")

      val caption = new Label(title) { styleClass = Seq("page-title") }
      AnchorPane.setLeftAnchor(caption, 0.0)
      AnchorPane.setBottomAnchor(caption, 0.0)

      children = Seq(caption)
    }
  }
}

object Page {
  case class Button(title: String, action: () => Unit)

  def btnCreate(action: => Unit = ()) = Button("Создать", () => action)
}