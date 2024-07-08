/*
 * Copyright 2003-2023 The IdeaVim authors
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE.txt file or at
 * https://opensource.org/licenses/MIT.
 */
package com.maddyhome.idea.vim.action.copy

import com.intellij.vim.annotations.CommandOrMotion
import com.intellij.vim.annotations.Mode
import com.maddyhome.idea.vim.api.ExecutionContext
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.command.Argument
import com.maddyhome.idea.vim.command.Command
import com.maddyhome.idea.vim.command.OperatorArguments
import com.maddyhome.idea.vim.handler.ChangeEditorActionHandler
import com.maddyhome.idea.vim.put.PutData
import com.maddyhome.idea.vim.put.PutData.TextData

sealed class PutTextBaseAction(
  private val insertTextBeforeCaret: Boolean,
  private val indent: Boolean,
  private val caretAfterInsertedText: Boolean,
) : ChangeEditorActionHandler.SingleExecution() {
  override val type: Command.Type = Command.Type.OTHER_SELF_SYNCHRONIZED

  override fun execute(
    editor: VimEditor,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Boolean {
    val count = operatorArguments.count1
    val sortedCarets = editor.sortedCarets()
    return if (sortedCarets.size > 1) {
      val putData = getPutData(count)

      val splitText = putData.textData?.rawText?.split('\n')?.dropLastWhile(String::isEmpty)
      val caretToPutData = if (splitText != null && splitText.size == sortedCarets.size) {
        sortedCarets.mapIndexed { index, caret -> caret to putData.copy(textData = putData.textData.copy(rawText = splitText[splitText.lastIndex - index])) }.toMap()
      } else {
        sortedCarets.associateWith { putData }
      }
      
      var result = true
      caretToPutData.forEach {
        result = injector.put.putTextForCaret(editor, it.key, context, it.value) && result
      }
      result
    } else {
      injector.put.putText(editor, context, getPutData(count))
    }
  }

  private fun getPutData(count: Int,
  ): PutData {
    return PutData(getRegisterTextData(), null, count, insertTextBeforeCaret, indent, caretAfterInsertedText, -1)
  }
}

fun getRegisterTextData(): TextData? {
  val register = injector.registerGroup.getRegister(injector.registerGroup.currentRegister)
  return register?.let {
    TextData(
      register.text ?: injector.parser.toPrintableString(register.keys),
      register.type,
      register.transferableData,
      register.name,
    )
  }
}

@CommandOrMotion(keys = ["p"], modes = [Mode.NORMAL])
class PutTextAfterCursorAction : PutTextBaseAction(insertTextBeforeCaret = false, indent = true, caretAfterInsertedText = false)

@CommandOrMotion(keys = ["gp"], modes = [Mode.NORMAL])
class PutTextAfterCursorActionMoveCursor : PutTextBaseAction(insertTextBeforeCaret = false, indent = true, caretAfterInsertedText = true)

@CommandOrMotion(keys = ["]p"], modes = [Mode.NORMAL])
class PutTextAfterCursorNoIndentAction : PutTextBaseAction(insertTextBeforeCaret = false, indent = false, caretAfterInsertedText = false)

@CommandOrMotion(keys = ["[P", "]P", "[p"], modes = [Mode.NORMAL])
class PutTextBeforeCursorNoIndentAction : PutTextBaseAction(insertTextBeforeCaret = true, indent = false, caretAfterInsertedText = false)

@CommandOrMotion(keys = ["P"], modes = [Mode.NORMAL])
class PutTextBeforeCursorAction : PutTextBaseAction(insertTextBeforeCaret = true, indent = true, caretAfterInsertedText = false)

@CommandOrMotion(keys = ["gP"], modes = [Mode.NORMAL])
class PutTextBeforeCursorActionMoveCursor : PutTextBaseAction(insertTextBeforeCaret = true, indent = true, caretAfterInsertedText = true)
