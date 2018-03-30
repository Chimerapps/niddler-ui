package com.icapps.niddler.ui.form

class NiddlerProcessImplementation : NiddlerConnectProcess {
    override fun getProcesses(): List<String> {
        return mutableListOf("be.icapps.project1", "be.icapps.project2", "be.icapps.project3")
    }
}