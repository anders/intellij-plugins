// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.model.store

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSNewExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.stubs.JSObjectLiteralExpressionStub
import com.intellij.openapi.vfs.VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.vuejs.codeInsight.objectLiteralFor
import org.jetbrains.vuejs.model.source.EntityContainerInfoProvider.InitializedContainerInfoProvider.BooleanValueAccessor

abstract class VuexContainerImpl : VuexContainer {

  override val actions: Map<String, VuexAction>
    get() = get(VuexContainerInfoProvider.VuexContainerInfo::actions)

  override val getters: Map<String, VuexGetter>
    get() = get(VuexContainerInfoProvider.VuexContainerInfo::getters)

  override val state: Map<String, VuexStateProperty>
    get() = get(VuexContainerInfoProvider.VuexContainerInfo::state)

  override val mutations: Map<String, VuexMutation>
    get() = get(VuexContainerInfoProvider.VuexContainerInfo::mutations)

  override val modules: Map<String, VuexModule>
    get() = get(VuexContainerInfoProvider.VuexContainerInfo::modules)

  private fun <T> get(accessor: (VuexContainerInfoProvider.VuexContainerInfo) -> Map<String, T>): Map<String, T> {
    return VuexContainerInfoProvider.INSTANCE.getInfo(initializer, null)?.let {
      accessor.invoke(it)
    } ?: mapOf()
  }

  protected fun get(accessor: (VuexContainerInfoProvider.VuexContainerInfo) -> Boolean): Boolean {
    return VuexContainerInfoProvider.INSTANCE.getInfo(initializer, null)?.let {
      accessor.invoke(it)
    } == true
  }

}

class VuexModuleImpl(override val name: String,
                     private val initializerElement: PsiElement,
                     nameElement: PsiElement) : VuexContainerImpl(), VuexModule {

  constructor(name: String, element: PsiElement) : this(name, element, element)

  override val source = nameElement

  override val resolveTarget: PsiElement = nameElement

  override val isNamespaced: Boolean
    get() = get(VuexContainerInfoProvider.VuexContainerInfo::isNamespaced)

  override val initializer: JSObjectLiteralExpression?
    get() {
      (initializerElement as? JSObjectLiteralExpression)?.let { return it }
      val initializerElement = initializerElement
      return CachedValuesManager.getCachedValue(initializerElement) {
        objectLiteralFor(initializerElement)?.let { create(it, initializerElement, it) }
        ?: create(null as JSObjectLiteralExpression?, initializerElement, VFS_STRUCTURE_MODIFICATIONS)
      }
    }
}

class VuexStoreImpl(override val source: JSNewExpression) : VuexContainerImpl(), VuexStore {
  override val initializer: JSObjectLiteralExpression?
    get() {
      val storeCreationCall = this.source
      return CachedValuesManager.getCachedValue(storeCreationCall) {
        readLiteralFromParams(storeCreationCall)
          ?.let { create(it, storeCreationCall, it) }
        ?: create(null as JSObjectLiteralExpression?, storeCreationCall, VFS_STRUCTURE_MODIFICATIONS)
      }
    }

  companion object {
    fun readLiteralFromParams(call: JSCallExpression): JSObjectLiteralExpression? {
      (call as? StubBasedPsiElementBase<*>)
        ?.stub
        ?.let {
          @Suppress("USELESS_CAST")
          return it.findChildStubByType<JSObjectLiteralExpression, JSObjectLiteralExpressionStub>(JSElementTypes.OBJECT_LITERAL_EXPRESSION)
            ?.psi as JSObjectLiteralExpression?
        }
      return call.arguments.getOrNull(0) as? JSObjectLiteralExpression
    }
  }
}

abstract class VuexNamedSymbolImpl(override val name: String,
                                   override val source: PsiElement) : VuexNamedSymbol

class VuexStatePropertyImpl(name: String, source: PsiElement)
  : VuexNamedSymbolImpl(name, source), VuexStateProperty {
  override val jsType: JSType? = null
}

class VuexActionImpl(name: String, source: PsiElement)
  : VuexNamedSymbolImpl(name, source), VuexAction {

  override val isRoot: Boolean get() = initializer?.let { IS_ROOT.build(it) } == true

  private val initializer: JSObjectLiteralExpression? get() {
    val initializerHolder = source
    return CachedValuesManager.getCachedValue(initializerHolder) {
      objectLiteralFor(initializerHolder)?.let { create(it, initializerHolder, it) }
      ?: create(null as JSObjectLiteralExpression?, initializerHolder, VFS_STRUCTURE_MODIFICATIONS)
    }
  }

  companion object {
    private val IS_ROOT = BooleanValueAccessor("root")
  }
}

class VuexGetterImpl(name: String, source: PsiElement)
  : VuexNamedSymbolImpl(name, source), VuexGetter {
  override val jsType: JSType? = null
}

class VuexMutationImpl(name: String, source: PsiElement)
  : VuexNamedSymbolImpl(name, source), VuexMutation
