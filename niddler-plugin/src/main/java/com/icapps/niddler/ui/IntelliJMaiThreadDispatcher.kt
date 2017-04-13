package com.icapps.niddler.ui

import com.icapps.niddler.ui.form.MainThreadDispatcher
import com.intellij.openapi.application.ApplicationManager

/**
 * @author Nicola Verbeeck
 * @date 21/11/16.
 */
class IntelliJMaiThreadDispatcher : MainThreadDispatcher {

    override fun dispatch(toExecute: Runnable) {
        ApplicationManager.getApplication().invokeLater(toExecute)
    }

}