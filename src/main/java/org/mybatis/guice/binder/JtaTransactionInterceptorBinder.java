package org.mybatis.guice.binder;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import static com.google.inject.matcher.Matchers.not;

import com.google.inject.Binder;

import javax.inject.Provider;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.mybatis.guice.transactional.Transactional;
import org.mybatis.guice.transactional.TransactionalMethodInterceptor;
import org.mybatis.guice.transactional.TxTransactionalMethodInterceptor;
import org.mybatis.guice.transactional.XASqlSessionManagerProvider;

public class JtaTransactionInterceptorBinder implements TransactionInterceptorBinder {

  private final Log log = LogFactory.getLog(getClass());

  private TransactionManager transactionManager;
  private Class<? extends Provider<? extends XAResource>> xaResourceProvider = XASqlSessionManagerProvider.class;
  
  public JtaTransactionInterceptorBinder(TransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public TransactionManager getTransactionManager() {
    return transactionManager;
  }

  protected void setTransactionManager(TransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }
  
  public void setXaResourceProvider(
      Class<? extends Provider<? extends XAResource>> xaResourceProvider) {
    this.xaResourceProvider = xaResourceProvider;
  }
  
  @Override
  public void bindTransactionInterceptors(final Binder binder) {
    TransactionManager manager = getTransactionManager();
    
    log.debug("bind XA transaction interceptors");

    // transactional interceptor
    TransactionalMethodInterceptor interceptor = new TransactionalMethodInterceptor();
    binder.requestInjection(interceptor);

    // jta transactional interceptor
    TxTransactionalMethodInterceptor interceptorTx = new TxTransactionalMethodInterceptor();
    binder.requestInjection(interceptorTx);
    binder.bind(XAResource.class).toProvider(xaResourceProvider);

    binder.bind(TransactionManager.class).toInstance(manager);

    binder.bindInterceptor(any(), not(DECLARED_BY_OBJECT).and(annotatedWith(Transactional.class)), interceptorTx,
        interceptor);
    // Intercept classes annotated with Transactional, but avoid "double"
    // interception when a mathod is also annotated inside an annotated
    // class.
    binder.bindInterceptor(annotatedWith(Transactional.class),
        not(DECLARED_BY_OBJECT).and(not(annotatedWith(Transactional.class))), interceptorTx, interceptor);
  }
}
