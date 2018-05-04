package com.icapps.niddler.ui.form.ui

import javax.swing.JTable
import javax.swing.JTree

/**
 * @author Nicola Verbeeck
 * @date 14/11/2017.
 */
interface NiddlerOverviewUserInterface {

    val messagesAsTable: JTable
    val messagesAsTree: JTree

    fun init()

    fun showTable()
    fun showLinked()
    fun showDebugView()

}