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
package org.mybatis.guice;

import static com.google.inject.name.Names.named;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Scopes;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.mybatis.guice.binder.DefaultTransactionInterceptorBinder;
import org.mybatis.guice.binder.TransactionInterceptorBinder;
import org.mybatis.guice.session.SqlSessionManagerProvider;

abstract class AbstractMyBatisModule extends AbstractModule {

  private ClassLoader resourcesClassLoader = getDefaultClassLoader();

  private ClassLoader driverClassLoader = getDefaultClassLoader();
  
  protected TransactionInterceptorBinder interceptorBinder = new DefaultTransactionInterceptorBinder();

  @Override
  protected final void configure() {
    try {
      // sql session manager
      bind(SqlSessionManager.class).toProvider(SqlSessionManagerProvider.class).in(Scopes.SINGLETON);
      bind(SqlSession.class).to(SqlSessionManager.class).in(Scopes.SINGLETON);

      internalConfigure();

      interceptorBinder.bindTransactionInterceptors(binder());

      bind(ClassLoader.class).annotatedWith(named("JDBC.driverClassLoader")).toInstance(driverClassLoader);
    } finally {
      resourcesClassLoader = getDefaultClassLoader();
      driverClassLoader = getDefaultClassLoader();
    }
  }

  /**
   * Use resource class loader.
   *
   * @param resourceClassLoader the resource class loader
   * @since 3.3
   */
  public void useResourceClassLoader(ClassLoader resourceClassLoader) {
    this.resourcesClassLoader = resourceClassLoader;
  }

  /**
   * Gets the resource class loader.
   *
   * @return the resource class loader
   * @since 3.3
   */
  protected final ClassLoader getResourceClassLoader() {
    return resourcesClassLoader;
  }

  /**
   * Use jdbc driver class loader.
   *
   * @param driverClassLoader the driver class loader
   * @since 3.3
   */
  public void useJdbcDriverClassLoader(ClassLoader driverClassLoader) {
    this.driverClassLoader = driverClassLoader;
  }

  /**
   * Gets the default class loader.
   *
   * @return the default class loader
   * @since 3.3
   */
  private ClassLoader getDefaultClassLoader() {
    return getClass().getClassLoader();
  }

  /**
   * Configures a {@link Binder} via the exposed methods.
   */
  abstract void internalConfigure();

  /**
   * Initialize.
   */
  protected abstract void initialize();

}
