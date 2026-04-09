package com.maddyhome.idea.vim.vimscript.model.functions.handlers

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.impl.PresentationFactory
import com.intellij.openapi.actionSystem.impl.Utils
import com.intellij.openapi.keymap.impl.ActionProcessor
import com.intellij.vim.annotations.VimscriptFunction
import com.maddyhome.idea.vim.api.ExecutionContext
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.newapi.ij
import com.maddyhome.idea.vim.vimscript.model.VimLContext
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimInt
import com.maddyhome.idea.vim.vimscript.model.datatypes.asVimInt
import com.maddyhome.idea.vim.vimscript.model.functions.BuiltinFunctionHandler
import java.awt.event.KeyEvent

@VimscriptFunction(name = "isactionenabled")
internal class IsActionEnabled : BuiltinFunctionHandler<VimInt>() {
  override fun doFunction(
    arguments: Arguments,
    editor: VimEditor,
    context: ExecutionContext,
    vimContext: VimLContext,
  ): VimInt {
    val action = ActionManager.getInstance().getAction(arguments.getString(0).value)
    if (action == null) {
      return false.asVimInt()
    }

    val presentationFactory = PresentationFactory()
    val wrappedContext = Utils.createAsyncDataContext(context.ij)
    val actionProcessor = object : ActionProcessor() {}
    val inputEventAdjusted = KeyEvent(editor.ij.contentComponent, KeyEvent.KEY_PRESSED, 0L, 0, KeyEvent.VK_UNDEFINED, '\u0000')

    val updateEvent = Utils.runUpdateSessionForInputEvent(listOf(action), inputEventAdjusted, wrappedContext, "IdeaVim", actionProcessor, presentationFactory) { _, updater, events ->
      val presentation = updater(action)
      events[presentation]
    }

    val result = updateEvent != null && updateEvent.presentation.isEnabled
    return result.asVimInt()
  }
}
