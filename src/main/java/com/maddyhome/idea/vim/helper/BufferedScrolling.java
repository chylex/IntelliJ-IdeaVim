package com.maddyhome.idea.vim.helper;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

final class BufferedScrolling {

  static final Key<BufferedScrolling> BUFFERED_SCROLLING_KEY = Key.create("BUFFERED_SCROLLING");

  static BufferedScrolling get(@NotNull Editor editor) {
    BufferedScrolling bufferedScrolling = editor.getUserData(BUFFERED_SCROLLING_KEY);
    if (bufferedScrolling == null) {
      editor.putUserData(BUFFERED_SCROLLING_KEY, bufferedScrolling = new BufferedScrolling(editor));
    }
    return bufferedScrolling;
  }

  private final Editor editor;

  private boolean isPending = false;
  private Integer targetX = null;
  private Integer targetY = null;

  BufferedScrolling(Editor editor) {
    this.editor = editor;
  }

  void scrollHorizontally(int offset) {
    targetX = offset;
    scrollLater();
  }

  void scrollVertically(int offset) {
    targetY = offset;
    scrollLater();
  }

  @SuppressWarnings("DataFlowIssue")
  private void scrollLater() {
    if (isPending) {
      return;
    }

    isPending = true;
    ApplicationManager.getApplication().invokeLater(() -> {
      isPending = false;
      Rectangle ref = targetX == null || targetY == null
                      ? editor.getScrollingModel().getVisibleAreaOnScrollingFinished()
                      : null;
      editor.getScrollingModel().scroll(targetX == null ? ref.x : targetX, targetY == null ? ref.y : targetY);
    });
  }
}
