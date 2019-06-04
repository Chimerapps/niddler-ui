package com.chimerapps.niddler.ui.util.adb

import com.icapps.niddler.lib.device.adb.ADBBootstrap
import com.icapps.niddler.lib.utils.info
import com.icapps.niddler.lib.utils.logger
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project

internal object ADBUtils {

    private val log = logger<ADBBootstrap>()

    fun guessPaths(project: Project): Collection<String> {
        val paths = HashSet<String>()
        val pathProperty = System.getProperty("android.sdk.path")
        if (pathProperty != null) {
            log.info("Got android sdk path from property: $pathProperty")
            paths += pathProperty
        }
        val fromProperties = PropertiesComponent.getInstance(project).getValue("android.sdk.path")
        if (fromProperties != null) {
            log.info("Got android sdk path from project properties: $fromProperties")
            paths += fromProperties
        }

        return paths
    }

}