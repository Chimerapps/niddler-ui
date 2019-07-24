package com.chimerapps.niddler.ui.component.view

import com.icapps.niddler.lib.model.BaseUrlHider
import com.icapps.niddler.lib.model.NiddlerMessageStorage
import com.icapps.niddler.lib.model.ParsedNiddlerMessage

interface MessagesView {

    var urlHider: BaseUrlHider?

    var filter: NiddlerMessageStorage.Filter<ParsedNiddlerMessage>?

    fun updateScrollToEnd(scrollToEnd: Boolean)

    fun onMessagesUpdated()

}