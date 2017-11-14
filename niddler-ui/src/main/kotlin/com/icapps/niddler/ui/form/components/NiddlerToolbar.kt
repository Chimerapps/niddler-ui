package com.icapps.niddler.ui.form.components

/**
 * @author Nicola Verbeeck
 * @date 14/11/2017.
 */
interface NiddlerToolbar {

    var listener: ToolbarListener?

    interface ToolbarListener {

        fun onTimelineSelected()

        fun onLinkedSelected()

        fun onClearSelected()

        fun onExportSelected()


    }

}