// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.impl.ProjectLifecycleListener
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.concurrency.NonUrgentExecutor
import training.lang.LangManager

internal class TrainingProjectLifecycleListener : ProjectLifecycleListener {
  override fun projectComponentsInitialized(project: Project) {
    NonUrgentExecutor.getInstance().execute {
      val langSupport = LangManager.getInstance().getLangSupport() ?: return@execute
      if (FileUtil.pathsEqual(project.basePath, NewLearnProjectUtil.projectFilePath(langSupport))) {
        langSupport.setProjectListeners(project)
      }
    }
  }
}