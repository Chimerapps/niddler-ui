package com.icapps.niddler.lib.export

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.NiddlerMessageStorage

/**
 * @author nicolaverbeeck
 */
interface Exporter<T : NiddlerMessage> {

    fun export(messages: NiddlerMessageStorage<T>, filter: NiddlerMessageStorage.Filter<T>?)

}