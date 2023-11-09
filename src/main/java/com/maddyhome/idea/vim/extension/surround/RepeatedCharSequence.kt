package com.maddyhome.idea.vim.extension.surround

import com.intellij.util.text.CharSequenceSubSequence

internal data class RepeatedCharSequence(val text: CharSequence, val count: Int) : CharSequence {
  override val length = text.length * count

  override fun get(index: Int): Char {
    if (index < 0 || index >= length) throw IndexOutOfBoundsException()
    return text[index % text.length]
  }

  override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
    return CharSequenceSubSequence(this, startIndex, endIndex)
  }

  override fun toString(): String {
    return text.repeat(count)
  }
  
  companion object {
    fun of(text: CharSequence, count: Int): CharSequence {
      return when (count) {
        0 -> ""
        1 -> text
        else -> RepeatedCharSequence(text, count)
      }
    }
  }
}
