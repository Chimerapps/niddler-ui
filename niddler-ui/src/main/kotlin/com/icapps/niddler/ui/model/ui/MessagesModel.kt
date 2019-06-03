package com.icapps.niddler.ui.model.ui

import com.icapps.niddler.lib.model.BaseUrlHider
import com.icapps.niddler.lib.model.NiddlerMessageStorage
import com.icapps.niddler.lib.model.ParsedNiddlerMessage

/**
 * @author Nicola Verbeeck
 * @date 02/05/2017.
 */
interface MessagesModel {

    fun updateMessages(messages: NiddlerMessageStorage<ParsedNiddlerMessage>, urlHider: BaseUrlHider)

}