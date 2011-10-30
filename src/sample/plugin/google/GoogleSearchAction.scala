package sample.plugin.google

import com.intellij.openapi.actionSystem.{PlatformDataKeys, AnAction, AnActionEvent}
import com.intellij.ide.BrowserUtil
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import scala.util.control.Exception._

class GoogleSearchAction extends AnAction {

  override def update(e: AnActionEvent) {
    e.getPresentation.setEnabled(e.getData(PlatformDataKeys.EDITOR) != null)
  }

  def actionPerformed(e: AnActionEvent) {
    val editor = e.getData(PlatformDataKeys.EDITOR)
    val editorText = editor.getDocument.getCharsSequence
    val offset = editor.getCaretModel.getOffset
    for {
      text <- getWordAtCursor(editorText, offset)
      query <- catching(classOf[UnsupportedEncodingException]) opt URLEncoder.encode(text, "UTF-8")
    } {
      BrowserUtil.launchBrowser("http://www.google.com/search?q=" + query)
    }
  }

  private def getWordAtCursor(editorText: CharSequence, cursorOffset: Int): Option[String] =
    editorText.length match {
      case 0 => None

      case length =>
        def isJavaIdentifierPart(index: Int) = Character.isJavaIdentifierPart(editorText.charAt(index))

        val offset =
          if (cursorOffset > 0 && !isJavaIdentifierPart(cursorOffset) && isJavaIdentifierPart(cursorOffset - 1))
            cursorOffset - 1
          else
            cursorOffset

        if (isJavaIdentifierPart(offset)) {
          val start = {
            var s = offset
            while (s > 0 && isJavaIdentifierPart(s - 1)) s -= 1
            s
          }
          val end = {
            var e = offset
            while (e < length && isJavaIdentifierPart(e)) e += 1
            e
          }
          Some(editorText.subSequence(start, end).toString)
        } else {
          None
        }
    }

}
