package com.chimerapps.niddler.ui.util.ui

import javax.swing.JCheckBox
import kotlin.reflect.KMutableProperty0

class CheckBox(label: String, initialValue: Boolean, val changeListener: (Boolean) -> Unit) : JCheckBox(label, initialValue) {

    constructor(label: String, property: KMutableProperty0<Boolean>) : this(label, property.get(), { property.set(it) })

    init {
        val model = getModel()
        model.addChangeListener {
            changeListener(model.isSelected)
        }
    }

}