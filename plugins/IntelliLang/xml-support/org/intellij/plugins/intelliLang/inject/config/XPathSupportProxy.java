/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.intellij.plugins.intelliLang.inject.config;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiFile;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Proxy class that allows to avoid a hard compile time dependency on the XPathView plugin.
 */
public abstract class XPathSupportProxy {
  private static final Logger LOG = Logger.getInstance("org.intellij.plugins.intelliLang.inject.config.XPathSupportProxy");

  public static final Object UNSUPPORTED = "UNSUPPORTED";
  public static final Object INVALID = "INVALID";

  @NotNull
  public abstract XPath createXPath(String expression) throws JaxenException;

  public abstract void attachContext(@NotNull PsiFile file);

  private static XPathSupportProxy ourInstance;
  private static boolean isInitialized;

  @Nullable
  public static synchronized XPathSupportProxy getInstance() {
    if (isInitialized) {
      return ourInstance;
    }
    try {
      return ourInstance = ServiceManager.getService(XPathSupportProxy.class);
    } finally {
      if (ourInstance == null) {
        LOG.info("XPath Support is not available");
      }
      isInitialized = true;
    }
  }

}