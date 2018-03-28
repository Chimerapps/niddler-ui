package com.icapps.niddler.ui.form.detail.body

import com.icapps.niddler.lib.model.ParsedNiddlerMessage


/**
 * @author Nicola Verbeeck
 * @date 15/11/16.
 */
class NiddlerHTMLDataPanel(message: ParsedNiddlerMessage) : NiddlerStructuredDataPanel(false, false, message) {


    init {
        initUI()
    }

}