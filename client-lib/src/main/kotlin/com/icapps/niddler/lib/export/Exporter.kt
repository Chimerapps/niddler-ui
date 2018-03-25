package com.icapps.niddler.lib.export

import com.icapps.niddler.lib.model.NiddlerMessageStorage

/**
 * @author nicolaverbeeck
 */
interface Exporter {

    fun export(messages: NiddlerMessageStorage)

}