package org.mybatis.guice.binder;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import static com.google.inject.matcher.Matchers.not;

import com.google.inject.Binder;

import org.mybatis.guice.transactional.Transactional;
import org.mybatis.guice.transactional.TransactionalMethodInterceptor;

public class DefaultTransactionInterceptorBinder implements TransactionInterceptorBinder {
  
  /**
   * bind transactional interceptors.
   */
  @Override
  public void bindTransactionInterceptors(final Binder binder) {
    // transactional interceptor
    TransactionalMethodInterceptor interceptor = new TransactionalMethodInterceptor();
    binder.requestInjection(interceptor);
    binder.bindInterceptor(any(), not(SYNTHETIC).and(not(DECLARED_BY_OBJECT)).and(annotatedWith(Transactional.class)),
        interceptor);
    // Intercept classes annotated with Transactional, but avoid "double"
    // interception when a mathod is also annotated inside an annotated
    // class.
    binder.bindInterceptor(annotatedWith(Transactional.class),
        not(SYNTHETIC).and(not(DECLARED_BY_OBJECT)).and(not(annotatedWith(Transactional.class))), interceptor);
  }
}
