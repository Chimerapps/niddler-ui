package com.chimerapps.niddler.ui.util.adb

import com.chimerapps.discovery.device.adb.ADBBootstrap
import com.chimerapps.discovery.utils.info
import com.chimerapps.discovery.utils.logger
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project

internal object ADBUtils {

    private val log = logger<ADBBootstrap>()

    fun guessPaths(project: Project?): Collection<String> {
        val paths = HashSet<String>()
        val pathProperty = System.getProperty("android.sdk.path")
        if (pathProperty != null) {
            log.info("Got android sdk path from property: $pathProperty")
            paths += pathProperty
        }
        project?.let {
            val fromProperties = PropertiesComponent.getInstance(project).getValue("android.sdk.path")
            if (fromProperties != null) {
                log.info("Got android sdk path from project properties: $fromProperties")
                paths += fromProperties
            }
        }
        return paths
    }

}