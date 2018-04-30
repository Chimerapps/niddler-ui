package com.icapps.niddler.ui.form.components

/**
 * @author Nicola Verbeeck
 * @date 14/11/2017.
 */
interface NiddlerMainToolbar {

    var listener: ToolbarListener?

    fun onBreakpointsMuted(muted: Boolean)

    interface ToolbarListener {

        fun onTimelineSelected()

        fun onLinkedSelected()

        fun onDebuggerViewSelected()

        fun onClearSelected()

        fun onExportSelected()

        fun onConfigureBreakpointsSelected()

        fun onMuteBreakpointsSelected()

    }

}