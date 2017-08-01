/**
 *    Copyright 2009-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.guice.configuration.settings;

import com.google.inject.name.Named;

import javax.annotation.Nullable;
import javax.inject.Provider;

import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;

public class StringBoundConfigurationSetting implements Provider<ConfigurationSetting> {

  @com.google.inject.Inject(optional = true)
  @Named("mybatis.configuration.lazyLoadingEnabled")
  private boolean lazyLoadingEnabled = false;

  @com.google.inject.Inject(optional = true)
  @Named("mybatis.configuration.aggressiveLazyLoading")
  private boolean aggressiveLazyLoading = true;

  @com.google.inject.Inject(optional = true)
  @Named("mybatis.configuration.multipleResultSetsEnabled")
  private boolean multipleResultSetsEnabled = true;

  @com.google.inject.Inject(optional = true)
  @Named("mybatis.configuration.useGeneratedKeys")
  private boolean useGeneratedKeys = false;

  @com.google.inject.Inject(optional = true)
  @Named("mybatis.configuration.useColumnLabel")
  private boolean useColumnLabel = true;

  @com.google.inject.Inject(optional = true)
  @Named("mybatis.configuration.cacheEnabled")
  private boolean cacheEnabled = true;

  @com.google.inject.Inject(optional = true)
  @Named("mybatis.configuration.defaultExecutorType")
  private ExecutorType defaultExecutorType = ExecutorType.SIMPLE;

  @com.google.inject.Inject(optional = true)
  @Named("mybatis.configuration.autoMappingBehavior")
  private AutoMappingBehavior autoMappingBehavior = AutoMappingBehavior.PARTIAL;

  @com.google.inject.Inject(optional = true)
  @Named("mybatis.configuration.callSettersOnNulls")
  private boolean callSettersOnNulls = false;

  @com.google.inject.Inject(optional = true)
  @Named("mybatis.configuration.defaultStatementTimeout")
  @Nullable
  private Integer defaultStatementTimeout;

  @com.google.inject.Inject(optional = true)
  @Named("mybatis.configuration.mapUnderscoreToCamelCase")
  private boolean mapUnderscoreToCamelCase = false;

  @Override
  public ConfigurationSetting get() {
    return new ConfigurationSetting() {
      @Override
      public void applyConfigurationSetting(Configuration configuration) {
        configuration.setLazyLoadingEnabled(lazyLoadingEnabled);
        configuration.setAggressiveLazyLoading(aggressiveLazyLoading);
        configuration.setMultipleResultSetsEnabled(multipleResultSetsEnabled);
        configuration.setUseGeneratedKeys(useGeneratedKeys);
        configuration.setUseColumnLabel(useColumnLabel);
        configuration.setCacheEnabled(cacheEnabled);
        configuration.setDefaultExecutorType(defaultExecutorType);
        configuration.setAutoMappingBehavior(autoMappingBehavior);
        configuration.setCallSettersOnNulls(callSettersOnNulls);
        configuration.setDefaultStatementTimeout(defaultStatementTimeout);
        configuration.setMapUnderscoreToCamelCase(mapUnderscoreToCamelCase);
      }
    };
  }
}
