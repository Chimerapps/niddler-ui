package com.icapps.niddler.ui.form.ui

import com.icapps.niddler.ui.form.TimelineJTable
import javax.swing.JTree

/**
 * @author Nicola Verbeeck
 * @date 14/11/2017.
 */
interface NiddlerOverviewUserInterface {

    val messagesAsTable: TimelineJTable
    val messagesAsTree: JTree

    fun init()

    fun showTable()
    fun showLinked()
    fun showDebugView()

}