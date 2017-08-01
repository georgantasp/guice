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
package org.mybatis.guice.configuration;

import com.google.inject.ProvisionException;
import com.google.inject.name.Named;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.mybatis.guice.configuration.settings.ConfigurationSetting;
import org.mybatis.guice.configuration.settings.MapperConfigurationSetting;

/**
 * Provides the myBatis Configuration.
 */
@Singleton
public class ConfigurationProvider implements Provider<Configuration> {

  /**
   * The myBatis Configuration reference.
   */
  private final Environment environment;

  @com.google.inject.Inject(optional = true) // Annotation left in place for backward compatibility
  @Named("mybatis.configuration.failFast") // Annotation left in place for backward compatibility
  private boolean failFast = false;

  private Set<ConfigurationSetting> configurationSettings = new HashSet<ConfigurationSetting>();
  private Set<MapperConfigurationSetting> mapperConfigurationSettings = new HashSet<MapperConfigurationSetting>();

  /**
   * @since 1.0.1
   */
  @com.google.inject.Inject
  public ConfigurationProvider(final Environment environment) {
    this.environment = environment;
  }

  @Deprecated
  public void setEnvironment(Environment environment) {
  }

  /**
   * Flag to check all statements are completed.
   *
   * @param failFast
   *          flag to check all statements are completed
   * @since 1.0.1
   */
  public void setFailFast(boolean failFast) {
    this.failFast = failFast;
  }

  public void addConfigurationSetting(ConfigurationSetting configurationSetting) {
    this.configurationSettings.add(configurationSetting);
  }

  public void addMapperConfigurationSetting(MapperConfigurationSetting mapperConfigurationSetting) {
    this.mapperConfigurationSettings.add((MapperConfigurationSetting) mapperConfigurationSetting);
  }

  @Override
  public Configuration get() {
    final Configuration configuration = new Configuration(environment);

    try {
      for (ConfigurationSetting setting : configurationSettings) {
        setting.applyConfigurationSetting(configuration);
      }

      for (MapperConfigurationSetting setting : mapperConfigurationSettings) {
        setting.applyConfigurationSetting(configuration);
      }

      if (failFast) {
        configuration.getMappedStatementNames();
      }
    } catch (Throwable cause) {
      throw new ProvisionException("An error occurred while building the org.apache.ibatis.session.Configuration",
          cause);
    } finally {
      ErrorContext.instance().reset();
    }

    return configuration;
  }
}
