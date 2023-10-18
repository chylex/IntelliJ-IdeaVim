/*
 * Copyright 2003-2023 The IdeaVim authors
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE.txt file or at
 * https://opensource.org/licenses/MIT.
 */

package com.maddyhome.idea.vim.vimscript.model.functions.handlers

import com.intellij.openapi.util.SystemInfoRt
import com.intellij.util.system.CpuArch
import com.intellij.vim.annotations.VimscriptFunction
import com.maddyhome.idea.vim.api.ExecutionContext
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.statistic.VimscriptState
import com.maddyhome.idea.vim.vimscript.model.VimLContext
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimDataType
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimInt
import com.maddyhome.idea.vim.vimscript.model.expressions.Expression
import com.maddyhome.idea.vim.vimscript.model.functions.FunctionHandler

@VimscriptFunction(name = "has")
internal class HasFunctionHandler : FunctionHandler() {
  override val minimumNumberOfArguments = 1
  override val maximumNumberOfArguments = 2

  private val supportedFeatures = Features.discover()

  override fun doFunction(
    argumentValues: List<Expression>,
    editor: VimEditor,
    context: ExecutionContext,
    vimContext: VimLContext,
  ): VimDataType {
    val feature = argumentValues[0].evaluate(editor, context, vimContext).asString()
    if (feature == "ide") {
      VimscriptState.isIDESpecificConfigurationUsed = true
    }
    return if (supportedFeatures.contains(feature)) {
      VimInt.ONE
    } else {
      VimInt.ZERO
    }
  }
  
  private object Features {
    fun discover(): Set<String> {
      val features = mutableSetOf("ide")
      collectOperatingSystemType(features)
      return features
    }
    
    private fun collectOperatingSystemType(target: MutableSet<String>) {
      if (SystemInfoRt.isWindows) {
        target.add("win32")
        if (CpuArch.CURRENT.width == 64) {
          target.add("win64")
        }
      }
      else if (SystemInfoRt.isLinux) {
        target.add("linux")
      }
      else if (SystemInfoRt.isMac) {
        target.add("mac")
        target.add("macunix")
        target.add("osx")
        target.add("osxdarwin")
      }
      else if (SystemInfoRt.isFreeBSD) {
        target.add("bsd")
      }
      else if (SystemInfoRt.isSolaris) {
        target.add("sun")
      }
      
      if (SystemInfoRt.isUnix) {
        target.add("unix")
      }
    }
  }
}
