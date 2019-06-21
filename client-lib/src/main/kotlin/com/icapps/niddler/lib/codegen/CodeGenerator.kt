package com.icapps.niddler.lib.codegen

import com.icapps.niddler.lib.model.ParsedNiddlerMessage

/**
 * @author Nicola Verbeeck
 */
interface CodeGenerator {

    fun generateRequestCode(request: ParsedNiddlerMessage): String

}