package com.icapps.niddler.ui.form

import javax.swing.tree.DefaultMutableTreeNode

class NiddlerConnectedProcessTreeNode(val processName: String) : DefaultMutableTreeNode() {
    override fun isLeaf(): Boolean {
        return true
    }
}
