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
package org.mybatis.guice.datasource.builtin;

import java.sql.SQLException;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.sql.DataSource;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;

/**
 * Provides the myBatis built-in UnpooledDataSource.
 */
public final class UnpooledDataSourceProvider implements Provider<DataSource> {

  /**
   * The UnpooledDataSource reference.
   */
  private final UnpooledDataSource unpooledDataSource;

  /**
   * Creates a new UnpooledDataSource using the needed parameter.
   *
   * @param driver The JDBC driver class.
   * @param url the database URL of the form <code>jdbc:subprotocol:subname</code>.
   * @param driverClassLoader ClassLoader to use to load JDBC driver class.
   */
  @Inject
  public UnpooledDataSourceProvider(final BuiltinDataSourceProviderArguments args) {
    if(args.driverClassLoader != null) {
      unpooledDataSource = new UnpooledDataSource(args.driverClassLoader, args.driver, args.url, null, null);
    }else {
      unpooledDataSource = new UnpooledDataSource(args.driver, args.url, null, null);
    }
  }

  /**
   * Sets the user.
   *
   * @param username the new user
   * @since 3.3
   */
  @com.google.inject.Inject(optional = true)
  public void setUser(@Named("JDBC.username") final String username) {
    unpooledDataSource.setUsername(username);
  }

  /**
   * Sets the password.
   *
   * @param password the new password
   * @since 3.3
   */
  @com.google.inject.Inject(optional = true)
  public void setPassword(@Named("JDBC.password") final String password) {
    unpooledDataSource.setPassword(password);
  }

  /**
   * Sets the auto commit.
   *
   * @param autoCommit the new auto commit
   */
  @com.google.inject.Inject(optional = true)
  public void setAutoCommit(@Named("JDBC.autoCommit") final boolean autoCommit) {
    unpooledDataSource.setAutoCommit(autoCommit);
  }

  /**
   * Sets the login timeout.
   *
   * @param loginTimeout the new login timeout
   */
  @com.google.inject.Inject(optional = true)
  public void setLoginTimeout(@Named("JDBC.loginTimeout") final int loginTimeout) {
    try {
      unpooledDataSource.setLoginTimeout(loginTimeout);
    } catch (SQLException e) {
      throw new RuntimeException("Impossible to set login timeout '" + loginTimeout + "' to Unpooled Data Source", e);
    }
  }

  @com.google.inject.Inject(optional = true)
  public void setDriverProperties(@Named("JDBC.driverProperties") final Properties driverProperties) {
    unpooledDataSource.setDriverProperties(driverProperties);
  }

  @Override
  public DataSource get() {
    return unpooledDataSource;
  }

}
