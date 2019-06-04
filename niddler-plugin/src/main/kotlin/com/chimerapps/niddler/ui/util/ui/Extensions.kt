package com.chimerapps.niddler.ui.util.ui

import javax.swing.JTable

/**
 * @param columnIndex   The index of the column to adjust
 * @param width The preferred with to set. Passing a negative value has no effect
 */
fun JTable.setColumnPreferredWidth(columnIndex: Int, width: Int) {
    if (width < 0) return
    val column = columnModel.getColumn(columnIndex) ?: return
    column.preferredWidth = width
}
