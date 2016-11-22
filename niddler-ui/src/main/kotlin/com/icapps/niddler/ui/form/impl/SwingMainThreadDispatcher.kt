package com.icapps.niddler.ui.form.impl

import com.icapps.niddler.ui.form.MainThreadDispatcher
import javax.swing.SwingUtilities

/**
 * @author Nicola Verbeeck
 * @date 21/11/16.
 */
class SwingMainThreadDispatcher : MainThreadDispatcher {

    override fun dispatch(toExecute: Runnable) {
        SwingUtilities.invokeLater(toExecute)
    }

}