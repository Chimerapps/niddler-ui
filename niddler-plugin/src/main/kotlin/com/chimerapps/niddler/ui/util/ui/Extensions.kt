package com.chimerapps.niddler.ui.util.ui

import javax.swing.JTable

fun JTable.setColumnPreferredWidth(columnIndex: Int, width: Int) {
    val column = columnModel.getColumn(columnIndex) ?: return
    column.preferredWidth = width
}
