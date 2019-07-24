package com.icapps.niddler.lib.export

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.NiddlerMessageStorage
import java.io.OutputStream

/**
 * @author Nicola Verbeeck
 */
interface Exporter<T : NiddlerMessage> {

    fun export(target: OutputStream, messages: NiddlerMessageStorage<T>, filter: NiddlerMessageStorage.Filter<T>?)

}