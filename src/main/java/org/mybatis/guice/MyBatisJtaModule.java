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

import static org.mybatis.guice.Preconditions.checkArgument;
import static org.mybatis.guice.Preconditions.checkState;

import javax.inject.Provider;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.mybatis.guice.binder.JtaTransactionInterceptorBinder;

public abstract class MyBatisJtaModule extends MyBatisModule {


  public MyBatisJtaModule() {
  }

  public MyBatisJtaModule(TransactionManager transactionManager) {
    this.interceptorBinder = new JtaTransactionInterceptorBinder(transactionManager);
  }

  protected TransactionManager getTransactionManager() {
    return ((JtaTransactionInterceptorBinder)this.interceptorBinder).getTransactionManager();
  }

  protected void bindDefaultTransactionProvider() {
    Class<? extends TransactionFactory> factoryType = getTransactionManager() == null ? JdbcTransactionFactory.class
        : ManagedTransactionFactory.class;

    bindTransactionFactoryType(factoryType);
  }

  protected void bindXAResourceProvider(Class<? extends Provider<? extends XAResource>> xaResourceProvider) {
    checkArgument(xaResourceProvider != null, "Parameter 'xaResourceProvider' must be not null");
    checkState(this.interceptorBinder instanceof JtaTransactionInterceptorBinder, "");
    ((JtaTransactionInterceptorBinder)this.interceptorBinder).setXaResourceProvider(xaResourceProvider);
  }
}
