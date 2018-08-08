@file:Suppress("NAME_SHADOWING")

package com.maddyhome.idea.vim.extension.multiplecursors

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.command.CommandState
import com.maddyhome.idea.vim.command.CommandState.Mode
import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.common.TextRange
import com.maddyhome.idea.vim.extension.VimExtensionFacade.putExtensionHandlerMapping
import com.maddyhome.idea.vim.extension.VimExtensionFacade.putKeyMapping
import com.maddyhome.idea.vim.extension.VimExtensionHandler
import com.maddyhome.idea.vim.extension.VimNonDisposableExtension
import com.maddyhome.idea.vim.group.MotionGroup
import com.maddyhome.idea.vim.helper.CaretData
import com.maddyhome.idea.vim.helper.StringHelper.parseKeys

private const val NEXT_OCCURRENCE = "<Plug>NextOccurrence"
private const val NOT_WHOLE_OCCURRENCE = "<Plug>NotWholeOccurrence"
private const val SKIP_OCCURRENCE = "<Plug>SkipOccurrence"
private const val REMOVE_OCCURRENCE = "<Plug>RemoveOccurrence"
private const val ALL_OCCURRENCES = "<Plug>AllOccurrences"

private class State private constructor() {
  var nextOffset = -1
  var firstRange: TextRange? = null

  private object Holder {
    val INSTANCE = State()
  }

  companion object {
    val instance: State by lazy { Holder.INSTANCE }
  }
}

private fun selectRange(editor: Editor, caret: Caret, startOffset: Int, endOffset: Int) {
  CaretData.setVisualStart(caret, startOffset)
  VimPlugin.getMotion().updateSelection(editor, caret, endOffset)
  editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
}

class VimMultipleCursorsExtension : VimNonDisposableExtension() {
  override fun getName() = "multiple-cursors"

  override fun initOnce() {
    putExtensionHandlerMapping(MappingMode.NVO, parseKeys(NEXT_OCCURRENCE), NextOccurrenceHandler(), false)
    putExtensionHandlerMapping(MappingMode.NVO, parseKeys(NOT_WHOLE_OCCURRENCE), NextOccurrenceHandler(false), false)
    putExtensionHandlerMapping(MappingMode.NO, parseKeys(ALL_OCCURRENCES), AllOccurrencesHandler(), false)
    putExtensionHandlerMapping(MappingMode.V, parseKeys(SKIP_OCCURRENCE), SkipOccurrenceHandler(), false)
    putExtensionHandlerMapping(MappingMode.V, parseKeys(REMOVE_OCCURRENCE), RemoveOccurrenceHandler(), false)

    putKeyMapping(MappingMode.NVO, parseKeys("<A-n>"), parseKeys(NEXT_OCCURRENCE), true)
    putKeyMapping(MappingMode.NVO, parseKeys("g<A-n>"), parseKeys(NOT_WHOLE_OCCURRENCE), true)
    putKeyMapping(MappingMode.V, parseKeys("<A-x>"), parseKeys(SKIP_OCCURRENCE), true)
    putKeyMapping(MappingMode.V, parseKeys("<A-p>"), parseKeys(REMOVE_OCCURRENCE), true)
  }

  private class NextOccurrenceHandler(val whole: Boolean = true) : VimExtensionHandler {
    override fun execute(editor: Editor, context: DataContext) {
      val commandState = CommandState.getInstance(editor)
      val state = State.instance
      if (editor.caretModel.caretCount == 1 && commandState.mode != Mode.VISUAL) {
        val caret = editor.caretModel.primaryCaret
        state.firstRange = VimPlugin.getMotion().getWordRange(editor, caret, 1, false, false)

        val firstRange = state.firstRange ?: return
        val startOffset = firstRange.startOffset
        val endOffset = firstRange.endOffset

        state.nextOffset = VimPlugin.getSearch().searchWord(editor, caret, 1, whole, 1)

        commandState.pushState(Mode.VISUAL, CommandState.SubMode.VISUAL_CHARACTER, MappingMode.VISUAL)
        selectRange(editor, caret, startOffset, endOffset)
        MotionGroup.moveCaret(editor, caret, endOffset, true)
      }
      else {
        val caret = editor.caretModel.addCaret(editor.offsetToVisualPosition(state.nextOffset), true) ?: return
        val range = VimPlugin.getMotion().getWordRange(editor, caret, 1, false, false)

        val firstRange = state.firstRange ?: return

        val startOffset = range.startOffset
        val endOffset = range.startOffset + firstRange.endOffset - firstRange.startOffset

        if (startOffset == firstRange.startOffset && endOffset == firstRange.endOffset) {
          editor.caretModel.removeCaret(caret)
//          TODO: no more matches notification
          return
        }

        state.nextOffset = VimPlugin.getSearch().searchNext(editor, caret, 1)

        selectRange(editor, caret, startOffset, endOffset)
        MotionGroup.moveCaret(editor, caret, endOffset, true)
      }
    }
  }

  private class AllOccurrencesHandler : VimExtensionHandler {
    override fun execute(editor: Editor, context: DataContext) {
      TODO("not implemented")
    }
  }

  private class SkipOccurrenceHandler : VimExtensionHandler {
    override fun execute(editor: Editor, context: DataContext) {
      val caret = editor.caretModel.primaryCaret
      val offset = VimPlugin.getSearch().searchWord(editor, caret, 1, true, 1)
      if (offset == -1) return

      caret.moveToOffset(offset)
      val state = State.instance
      val range = VimPlugin.getMotion().getWordRange(editor, caret, 1, false, false)
      if (editor.caretModel.caretCount == 1) {
        state.firstRange = range
      }
      val startOffset = range.startOffset
      val endOffset = range.endOffset

      state.nextOffset = VimPlugin.getSearch().searchWord(editor, caret, 1, true, 1)

      selectRange(editor, caret, startOffset, endOffset)
      MotionGroup.moveCaret(editor, caret, endOffset, true)
    }
  }

  private class RemoveOccurrenceHandler : VimExtensionHandler {
    override fun execute(editor: Editor, context: DataContext) {
      State.instance.nextOffset = CaretData.getVisualStart(editor.caretModel.primaryCaret)

      editor.selectionModel.removeSelection()
      if (!editor.caretModel.removeCaret(editor.caretModel.primaryCaret)) {
        CommandState.getInstance(editor).popState()
      }
    }
  }
}