package com.maddyhome.idea.vim.action.macro

import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.command.impl.FinishMarkAction
import com.intellij.openapi.command.impl.StartMarkAction
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.vim.annotations.CommandOrMotion
import com.intellij.vim.annotations.Mode
import com.maddyhome.idea.vim.KeyHandler
import com.maddyhome.idea.vim.api.ExecutionContext
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.command.Argument
import com.maddyhome.idea.vim.command.Command
import com.maddyhome.idea.vim.command.OperatorArguments
import com.maddyhome.idea.vim.handler.VimActionHandler
import com.maddyhome.idea.vim.newapi.ij
import com.maddyhome.idea.vim.newapi.vim

@CommandOrMotion(keys = ["z@"], modes = [Mode.NORMAL])
class PlaybackRegisterInOpenFilesAction : VimActionHandler.SingleExecution() {
  override val type: Command.Type = Command.Type.OTHER_SELF_SYNCHRONIZED

  override val argumentType: Argument.Type = Argument.Type.CHARACTER

  private val playbackRegisterAction = PlaybackRegisterAction()
  
  override fun execute(
    editor: VimEditor,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments,
  ): Boolean {
    val argument = cmd.argument as? Argument.Character ?: return false

    val project = editor.ij.project ?: return false
    val fileEditorManager = FileEditorManagerEx.getInstanceExIfCreated(project) ?: return false

    val register = argument.character.let { if (it == '@') injector.macro.lastRegister else it }
    val commandName = "Execute Macro '$register' in All Open Files"

    val action = Runnable {
      CommandProcessor.getInstance().markCurrentCommandAsGlobal(project)
      
      for (textEditor in fileEditorManager.allEditors.filterIsInstance<TextEditor>()) {
        fileEditorManager.openFile(textEditor.file, true)

        val editor = textEditor.editor
        val vimEditor = editor.vim

        vimEditor.mode = com.maddyhome.idea.vim.state.mode.Mode.NORMAL()
        KeyHandler.Companion.getInstance().reset(vimEditor)

        val startMarkAction = StartMarkAction.start(editor, project, commandName)
        playbackRegisterAction.execute(vimEditor, context, cmd, operatorArguments)
        FinishMarkAction.finish(project, editor, startMarkAction)
      }
    }

    CommandProcessor.getInstance()
      .executeCommand(project, action, commandName, null, UndoConfirmationPolicy.REQUEST_CONFIRMATION)

    return true
  }
}
