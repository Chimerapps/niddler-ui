package com.icapps.niddler.ui.form.components

/**
 * @author nicolaverbeeck
 */
interface Dialog {

    companion object {
        const val ACCEPTED: Int = 0
        const val CANCELLED: Int = 1
    }

    var dialogCanResize: Boolean

    fun show(inputValidator: () -> ValidationResult): Int

    fun tryValidate()

    data class ValidationResult(val success: Boolean, val error: String? = null)

}