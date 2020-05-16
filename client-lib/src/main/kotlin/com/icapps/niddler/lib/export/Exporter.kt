package com.icapps.niddler.lib.export

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.NiddlerMessageContainer
import com.icapps.niddler.lib.model.storage.NiddlerMessageStorage
import java.io.OutputStream

/**
 * @author Nicola Verbeeck
 */
interface Exporter {

    fun export(target: OutputStream, messages: NiddlerMessageContainer, filter: NiddlerMessageStorage.Filter?)

}