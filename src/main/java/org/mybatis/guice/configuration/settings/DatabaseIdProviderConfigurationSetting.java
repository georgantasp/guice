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

import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.session.Configuration;

public class DatabaseIdProviderConfigurationSetting implements Provider<ConfigurationSetting> {

  @Inject
  DataSource dataSource;
  @Inject
  DatabaseIdProvider provider;

  @Override
  public ConfigurationSetting get() {
    return new ConfigurationSetting() {

      @Override
      public void applyConfigurationSetting(Configuration configuration) {
        try {
          configuration.setDatabaseId(provider.getDatabaseId(dataSource));
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

}
