package com.icapps.niddler.ui.form.ui

import com.icapps.niddler.lib.model.NiddlerMessageContainer
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.icapps.niddler.ui.form.ComponentsFactory
import com.icapps.niddler.ui.form.components.NiddlerMainToolbar
import com.icapps.niddler.ui.form.debug.view.DebugView
import javax.swing.JComponent

/**
 * @author Nicola Verbeeck
 * @date 14/11/2017.
 */

interface NiddlerUserInterface {

    fun init(messageContainer: NiddlerMessageContainer<ParsedNiddlerMessage>)

    fun selectMessage(parsedNiddlerMessage: ParsedNiddlerMessage)

    var connectButtonListener: (() -> Unit)?
    var filterListener: ((String?) -> Unit)?
    var disconnectButtonListener: (() -> Unit)?
    var debugButtonListener: (() -> Unit)?

    val toolbar: NiddlerMainToolbar
    val disconnectButton: AbstractAction

    val statusBar: NiddlerStatusbar

    val asComponent: JComponent
    val componentsFactory: ComponentsFactory

    val overview: NiddlerOverviewUserInterface
    val detail: NiddlerDetailUserInterface
    val debugView: DebugView
}
