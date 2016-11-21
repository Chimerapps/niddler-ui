import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

/**
 * Created by maartenvangiel on 21/11/2016.
 */
class SimpleAction : AnAction("Niddler"){

    override fun actionPerformed(e: AnActionEvent?) {
        Messages.showDialog("jhQKJDHDSKJFH", "sdkjfhdksjfh", arrayOf("OK"), -1, null)
    }

}