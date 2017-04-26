package me.demongoo.merchapp.pages

import me.demongoo.merchapp.gui.Page

class Index extends Page {
  override val title: String = "Главная"
  override val buttons: Seq[Page.Button] = Seq(Page.btnCreate(println("I'm clicked")))
}