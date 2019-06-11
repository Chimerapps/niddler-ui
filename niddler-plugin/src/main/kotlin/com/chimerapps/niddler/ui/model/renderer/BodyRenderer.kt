package com.chimerapps.niddler.ui.model.renderer

import com.chimerapps.niddler.ui.model.renderer.impl.json.JsonBodyRenderer
import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.BodyFormatType
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import javax.swing.JComponent

interface BodyRenderer<T : ParsedNiddlerMessage> {

    val supportsStructure: Boolean
    val supportsPretty: Boolean
    val supportsRaw: Boolean

    fun structured(message: T, reuseComponent: JComponent?): JComponent
    fun pretty(message: T, reuseComponent: JComponent?): JComponent
    fun raw(message: T, reuseComponent: JComponent?): JComponent

}

//TODO extensions
fun bodyRendererForFormat(format: BodyFormat): BodyRenderer<ParsedNiddlerMessage>? {
    return when (format.type) {
        BodyFormatType.FORMAT_JSON -> JsonBodyRenderer
        else -> null
    }
}