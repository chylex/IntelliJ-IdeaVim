/*
 * Copyright 2003-2023 The IdeaVim authors
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE.txt file or at
 * https://opensource.org/licenses/MIT.
 */
package com.maddyhome.idea.vim.action.motion.leftright

import com.intellij.vim.annotations.CommandOrMotion
import com.intellij.vim.annotations.Mode
import com.maddyhome.idea.vim.api.ExecutionContext
import com.maddyhome.idea.vim.api.ImmutableVimCaret
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.api.VimMotionGroupBase
import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.api.options
import com.maddyhome.idea.vim.command.Argument
import com.maddyhome.idea.vim.command.MotionType
import com.maddyhome.idea.vim.command.OperatorArguments
import com.maddyhome.idea.vim.handler.Motion
import com.maddyhome.idea.vim.handler.MotionActionHandler
import com.maddyhome.idea.vim.helper.isEndAllowed
import com.maddyhome.idea.vim.state.mode.inVisualMode
import com.maddyhome.idea.vim.state.mode.isInsertionAllowed

abstract class MotionLastScreenColumnBaseAction(private val isMotionForOperator: Boolean = false) : MotionActionHandler.ForEachCaret() {
  override fun getOffset(
    editor: VimEditor,
    caret: ImmutableVimCaret,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Motion {
    val allow = if (editor.isInsertionAllowed) {
      true
    } else if (editor.inVisualMode) {
      injector.options(editor).selection != "old"
    } else {
      !isMotionForOperator && editor.isEndAllowed
    }
    val motion = injector.motion.moveCaretToCurrentDisplayLineEnd(editor, caret, allow)
    return when (motion) {
      is Motion.AbsoluteOffset -> Motion.AdjustedOffset(motion.offset, VimMotionGroupBase.LAST_COLUMN)
      else -> motion
    }
  }

  override val motionType: MotionType = MotionType.INCLUSIVE
}

@CommandOrMotion(
  keys = ["g$", "g<End>"],
  modes = [Mode.NORMAL, Mode.VISUAL],
  description = "Move to the last character of the screen line. Differs from the end of the line when lines wrap."
)
class MotionLastScreenColumnAction : MotionLastScreenColumnBaseAction(isMotionForOperator = false)

@CommandOrMotion(
  keys = ["g$", "g<End>"],
  modes = [Mode.OP_PENDING],
  description = "Move to the last character of the screen line. Differs from the end of the line when lines wrap."
)
class MotionLastScreenColumnOpPendingAction : MotionLastScreenColumnBaseAction(isMotionForOperator = true)
