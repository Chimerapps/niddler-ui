package com.icapps.niddler.ui.codegen

import com.icapps.niddler.lib.model.ParsedNiddlerMessage

/**
 * @author Nicola Verbeeck
 * @date 14/06/2017.
 */
interface CodeGenerator {

    fun generateRequestCode(request: ParsedNiddlerMessage): String

}