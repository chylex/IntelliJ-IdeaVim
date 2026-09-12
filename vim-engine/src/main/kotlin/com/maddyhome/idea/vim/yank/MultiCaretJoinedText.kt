package com.maddyhome.idea.vim.yank

import com.maddyhome.idea.vim.common.VimCopiedText

data class MultiCaretJoinedText(val carets: Int, override val text: String): VimCopiedText {
  companion object {
    val EMPTY = MultiCaretJoinedText(0, "")
  }

  override fun updateText(newText: String): VimCopiedText {
    return MultiCaretJoinedText(carets, newText)
  }
}
