package com.icapps.niddler.lib.codegen

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.model.ParsedNiddlerMessage

/**
 * @author Nicola Verbeeck
 */
interface CodeGenerator {

    fun generateRequestCode(request: NiddlerMessage): String

}