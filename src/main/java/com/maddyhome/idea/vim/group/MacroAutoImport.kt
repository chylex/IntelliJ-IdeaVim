package com.maddyhome.idea.vim.group

import com.intellij.codeInsight.daemon.ReferenceImporter
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import java.util.function.BooleanSupplier

internal object MacroAutoImport {
  fun run(editor: Editor, dataContext: DataContext) {
    val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return
    val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return

    if (!FileDocumentManager.getInstance().requestWriting(editor.document, project)) {
      return
    }

    val importers = ReferenceImporter.EP_NAME.extensionList
    if (importers.isEmpty()) {
      return
    }

    ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Auto import", true) {
      override fun run(indicator: ProgressIndicator) {
        val fixes = ReadAction.nonBlocking<List<BooleanSupplier>> {
          val fixes = mutableListOf<BooleanSupplier>()

          file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
              for (reference in element.references) {
                if (reference.resolve() != null) {
                  continue
                }
                for (importer in importers) {
                  importer.computeAutoImportAtOffset(editor, file, element.textRange.startOffset, true)
                    ?.let(fixes::add)
                }
              }
              super.visitElement(element)
            }
          })

          return@nonBlocking fixes
        }.executeSynchronously()

        ApplicationManager.getApplication().invokeAndWait {
          WriteCommandAction.writeCommandAction(project)
            .withName("Auto Import")
            .withGroupId("IdeaVimAutoImportAfterMacro")
            .shouldRecordActionForActiveDocument(true)
            .run<RuntimeException> {
              fixes.forEach { it.asBoolean }
            }
        }
      }
    })
  }
}
