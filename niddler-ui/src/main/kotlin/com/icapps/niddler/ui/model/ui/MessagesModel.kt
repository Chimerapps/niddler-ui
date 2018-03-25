package com.icapps.niddler.ui.model.ui

import com.icapps.niddler.lib.model.NiddlerMessageStorage

/**
 * @author Nicola Verbeeck
 * @date 02/05/2017.
 */
interface MessagesModel {

    fun updateMessages(messages: NiddlerMessageStorage)

}