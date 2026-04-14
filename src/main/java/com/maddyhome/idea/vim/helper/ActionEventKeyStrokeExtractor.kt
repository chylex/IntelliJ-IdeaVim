/*
 * Copyright 2003-2026 The IdeaVim authors
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE.txt file or at
 * https://opensource.org/licenses/MIT.
 */

package com.maddyhome.idea.vim.helper

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.openapi.util.registry.Registry
import com.intellij.ui.KeyStrokeAdapter
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

/**
 * Extracts the [KeyStroke] of the key event that triggered an [AnActionEvent].
 */
internal class ActionEventKeyStrokeExtractor {
  private var keyStrokeCache: Pair<Long?, KeyStroke?> = null to null

  fun getKeyStroke(e: AnActionEvent): KeyStroke? {
    val inputEvent = e.inputEvent as? KeyEvent ?: return null
    val defaultKeyStroke = KeyStrokeAdapter.getDefaultKeyStroke(inputEvent)
    val strokeCache = keyStrokeCache
    if (defaultKeyStroke != null) {
      val fixedKeyStroke = fixKeyStroke(defaultKeyStroke)
      keyStrokeCache = inputEvent.`when` to fixedKeyStroke
      return fixedKeyStroke
    } else if (strokeCache.first == inputEvent.`when`) {
      keyStrokeCache = null to null
      return strokeCache.second
    }
    return KeyStroke.getKeyStrokeForEvent(inputEvent)
  }

  private fun fixKeyStroke(key: KeyStroke): KeyStroke {
    return if (
      key.modifiers and CTRL_ALT_MASK != 0 &&
      key.isOnKeyRelease &&
      SystemInfoRt.isWindows &&
      Registry.`is`("actionSystem.fix.alt.gr", true)
    ) {
      KeyStroke.getKeyStroke(key.keyCode, key.modifiers)
    } else {
      key
    }
  }

  companion object {
    private const val CTRL_ALT_MASK = InputEvent.CTRL_DOWN_MASK or InputEvent.ALT_DOWN_MASK
  }
}
