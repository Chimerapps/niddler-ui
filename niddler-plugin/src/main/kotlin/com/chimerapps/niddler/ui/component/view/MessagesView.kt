package com.chimerapps.niddler.ui.component.view

import com.icapps.niddler.lib.model.BaseUrlHider
import javax.swing.JComponent

interface MessagesView {

    var urlHider: BaseUrlHider?

    fun updateScrollToEnd(scrollToEnd: Boolean)

    fun onMessagesUpdated()

}