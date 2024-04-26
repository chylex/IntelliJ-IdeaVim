package com.maddyhome.idea.vim.extension.nerdtree

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service

@Service
internal class NerdTreeDisposableService : Disposable {
  override fun dispose() {}
}
