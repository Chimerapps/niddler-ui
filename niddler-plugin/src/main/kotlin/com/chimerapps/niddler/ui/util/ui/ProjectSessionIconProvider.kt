package com.chimerapps.niddler.ui.util.ui

import com.chimerapps.discovery.ui.DefaultSessionIconProvider
import com.chimerapps.discovery.ui.SessionIconProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.util.IconUtil
import java.lang.Float.min
import javax.swing.Icon
import javax.swing.ImageIcon

class ProjectSessionIconProvider private constructor(private val project: Project,
                                                     private val delegate: SessionIconProvider) : SessionIconProvider {

    companion object {
        private val projectInstances = mutableMapOf<Project, ProjectSessionIconProvider>()

        fun instance(project: Project, delegate: SessionIconProvider = DefaultSessionIconProvider()): ProjectSessionIconProvider {
            return projectInstances.getOrPut(project) { ProjectSessionIconProvider(project, delegate) }
        }
    }

    private val cache = mutableMapOf<String, Icon?>()

    override fun iconForString(iconString: String): Icon? {
        if (!cache.containsKey(iconString)) {
            initFromProject(iconString)
        }
        return cache[iconString] ?: delegate.iconForString(iconString)
    }

    private fun initFromProject(iconString: String) {
        val icon = loadFromProject(iconString)?.let {
            IconUtil.scale(it, null, min(20.0f / it.iconWidth, 20.0f / it.iconHeight))
        }
        cache[iconString] = icon
    }

    private fun loadFromProject(iconString: String): Icon? {
        val dir = project.guessProjectDir() ?: return null
        val matches = dir.findChild(".idea")?.findChild("niddler")?.children?.filter { file ->
            !file.isDirectory && (file.nameWithoutExtension == iconString || file.nameWithoutExtension == "$iconString@2x") && file.extension != "svg"
        } ?: return null
        if (matches.isEmpty()) return null

        matches.filter { it.nameWithoutExtension.endsWith("@2x") }.forEach { return ImageIcon(it.path) }
        matches.forEach { return ImageIcon(it.path) }

        return null
    }

}