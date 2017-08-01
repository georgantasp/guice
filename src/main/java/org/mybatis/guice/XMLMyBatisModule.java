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

import static com.google.inject.util.Providers.guicify;
import static org.apache.ibatis.io.Resources.getResourceAsReader;
import static org.mybatis.guice.Preconditions.checkArgument;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Properties;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.guice.binder.DefaultTransactionInterceptorBinder;
import org.mybatis.guice.binder.TransactionInterceptorBinder;
import org.mybatis.guice.mappers.MapperProvider;
import org.mybatis.guice.session.SqlSessionManagerProvider;

/**
 * Easy to use helper Module that alleviates users to write the boilerplate
 * google-guice bindings to create the SqlSessionFactory, via XML configuration.
 */
public abstract class XMLMyBatisModule extends AbstractModule {

  private static final String DEFAULT_CONFIG_RESOURCE = "mybatis-config.xml";

  private static final String DEFAULT_ENVIRONMENT_ID = null;

  private String classPathResource = DEFAULT_CONFIG_RESOURCE;

  private String environmentId = DEFAULT_ENVIRONMENT_ID;

  private Properties properties = new Properties();

  /**
   * Set the MyBatis configuration class path resource.
   *
   * @param classPathResource the MyBatis configuration class path resource
   */
  protected final void setClassPathResource(String classPathResource) {
    checkArgument(classPathResource != null, "Parameter 'classPathResource' must be not null");
    this.classPathResource = classPathResource;
  }

  /**
   * Set the MyBatis configuration environment id.
   *
   * @param environmentId the MyBatis configuration environment id
   */
  protected final void setEnvironmentId(String environmentId) {
    this.environmentId = environmentId;
  }

  /**
   * Add the variables will be used to replace placeholders in the MyBatis configuration.
   *
   * @param properties the variables will be used to replace placeholders in the MyBatis configuration
   */
  protected final void addProperties(Properties properties) {
    if (properties != null) {
      this.properties.putAll(properties);
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

  protected TransactionInterceptorBinder interceptorBinder = new DefaultTransactionInterceptorBinder();
  private ClassLoader resourcesClassLoader = getClass().getClassLoader();
  
  protected abstract void initialize();
  
  @Override
  protected final void configure() {
    this.initialize();

    Reader reader = null;
    try {
      reader = getResourceAsReader(resourcesClassLoader, classPathResource);
      SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(reader, environmentId, properties);
      bind(SqlSessionFactory.class).toInstance(sessionFactory);
      bind(SqlSessionManager.class).toProvider(SqlSessionManagerProvider.class).in(Scopes.SINGLETON);
      bind(SqlSession.class).to(SqlSessionManager.class).in(Scopes.SINGLETON);

      Configuration configuration = sessionFactory.getConfiguration();

      // bind mappers
      Collection<Class<?>> mapperClasses = configuration.getMapperRegistry().getMappers();
      for (Class<?> mapperType : mapperClasses) {
        bindMapper(mapperType);
      }

      // request injection for type handlers
      Collection<TypeHandler<?>> allTypeHandlers = configuration.getTypeHandlerRegistry().getTypeHandlers();
      for (TypeHandler<?> handler : allTypeHandlers) {
        requestInjection(handler);
      }

      // request injection for interceptors
      Collection<Interceptor> interceptors = configuration.getInterceptors();
      for (Interceptor interceptor : interceptors) {
        requestInjection(interceptor);
      }

      // request injection for object factory.
      requestInjection(configuration.getObjectFactory());

      // request injection for object wrapper factory.
      requestInjection(configuration.getObjectWrapperFactory());
    } catch (Exception e) {
      addError("Impossible to read classpath resource '%s', see nested exceptions: %s", classPathResource,
          e.getMessage());
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          // close quietly
        }
      }
    }

    interceptorBinder.bindTransactionInterceptors(binder());
  }

  private final <T> void bindMapper(Class<T> mapperType) {
    bind(mapperType).toProvider(guicify(new MapperProvider<T>(mapperType))).in(Scopes.SINGLETON);
  }
}
