package com.maddyhome.idea.vim.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.DumbAwareAction
import com.maddyhome.idea.vim.KeyHandler
import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.newapi.IjEditorExecutionContext
import com.maddyhome.idea.vim.newapi.vim
import com.maddyhome.idea.vim.state.mode.Mode

class VimRunLastMacroInOpenFiles : DumbAwareAction() {
  override fun update(e: AnActionEvent) {
    val lastRegister = injector.macro.lastRegister
    val isEnabled = lastRegister != 0.toChar()

    e.presentation.isEnabled = isEnabled
    e.presentation.text = if (isEnabled) "Run Macro '${lastRegister}' in Open Files" else "Run Last Macro in Open Files"
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.EDT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val fileEditorManager = FileEditorManagerEx.getInstanceExIfCreated(project) ?: return
    val editors = fileEditorManager.allEditors.filterIsInstance<TextEditor>()
    
    WriteCommandAction.writeCommandAction(project)
      .withName(e.presentation.text)
      .withGlobalUndo()
      .withUndoConfirmationPolicy(UndoConfirmationPolicy.REQUEST_CONFIRMATION)
      .run<RuntimeException> {
        val reg = injector.macro.lastRegister
        
        for (editor in editors) {
          fileEditorManager.openFile(editor.file, true)
          
          val vimEditor = editor.editor.vim
          vimEditor.mode = Mode.NORMAL()
          KeyHandler.getInstance().reset(vimEditor)
          
          injector.macro.playbackRegister(vimEditor, IjEditorExecutionContext(e.dataContext), reg, 1)
        }
      }
  }
}
