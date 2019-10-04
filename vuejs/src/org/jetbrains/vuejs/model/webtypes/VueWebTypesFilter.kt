// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.openapi.project.Project
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueGlobal
import org.jetbrains.vuejs.model.webtypes.json.VueFilter

class VueWebTypesFilter(filter: VueFilter,
                        project: Project,
                        private val parent: VueEntitiesContainer,
                        sourceSymbolResolver: WebTypesSourceSymbolResolver)
  : VueWebTypesSourceEntity(project, filter.source, sourceSymbolResolver), org.jetbrains.vuejs.model.VueFilter {

  override val global: VueGlobal? get() = parent.global
  override val parents: List<VueEntitiesContainer> get() = listOf(parent)

  override val defaultName: String = filter.name!!

}
