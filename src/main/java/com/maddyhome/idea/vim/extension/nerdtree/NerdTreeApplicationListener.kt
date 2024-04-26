package com.maddyhome.idea.vim.extension.nerdtree

import com.intellij.ide.ApplicationInitializedListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.ui.StartupUiUtil
import kotlinx.coroutines.CoroutineScope
import java.awt.AWTEvent
import java.awt.event.FocusEvent
import javax.swing.JTree

@Suppress("UnstableApiUsage")
internal class NerdTreeApplicationListener : ApplicationInitializedListener {
  override suspend fun execute(asyncScope: CoroutineScope) {
    StartupUiUtil.addAwtListener(::handleEvent, AWTEvent.FOCUS_EVENT_MASK, ApplicationManager.getApplication().getService(NerdTreeDisposableService::class.java))
  }

  private fun handleEvent(event: AWTEvent) {
    if (event is FocusEvent && event.id == FocusEvent.FOCUS_GAINED) {
      val source = event.source
      if (source is JTree) {
        NerdTree.installDispatcher(source)
      }
    }
  }
}
