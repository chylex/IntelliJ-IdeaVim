/*
 * Copyright 2003-2023 The IdeaVim authors
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE.txt file or at
 * https://opensource.org/licenses/MIT.
 */

package com.maddyhome.idea.vim.vimscript.model.functions.handlers

import com.intellij.refactoring.rename.inplace.InplaceRefactoring
import com.intellij.vim.annotations.VimscriptFunction
import com.maddyhome.idea.vim.api.ExecutionContext
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.newapi.ij
import com.maddyhome.idea.vim.vimscript.model.VimLContext
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimInt
import com.maddyhome.idea.vim.vimscript.model.datatypes.asVimInt
import com.maddyhome.idea.vim.vimscript.model.functions.BuiltinFunctionHandler

@VimscriptFunction(name = "isinplacerenaming")
internal class IsInPlaceRenaming : BuiltinFunctionHandler<VimInt>() {
  override fun doFunction(arguments: Arguments, editor: VimEditor, context: ExecutionContext, vimContext: VimLContext) =
    (InplaceRefactoring.getActiveInplaceRenamer(editor.ij) != null).asVimInt()
}
