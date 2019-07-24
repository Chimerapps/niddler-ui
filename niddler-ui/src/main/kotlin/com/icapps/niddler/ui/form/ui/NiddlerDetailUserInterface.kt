package com.icapps.niddler.ui.form.ui

import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import java.awt.Component

/**
 * @author Nicola Verbeeck
 * @date 14/11/2017.
 */
interface NiddlerDetailUserInterface {

    var message: ParsedNiddlerMessage?

    val asComponent: Component

    fun init()

}