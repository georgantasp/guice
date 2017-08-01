package org.mybatis.guice.binder;

import com.google.inject.Binder;
import com.google.inject.matcher.AbstractMatcher;

import java.lang.reflect.Method;

public interface TransactionInterceptorBinder {

  public static final AbstractMatcher<Method> DECLARED_BY_OBJECT = new AbstractMatcher<Method>() {
    @Override
    public boolean matches(Method method) {
      return method.getDeclaringClass() == Object.class;
    }
  };

  public static final AbstractMatcher<Method> SYNTHETIC = new AbstractMatcher<Method>() {
    @Override
    public boolean matches(Method method) {
      return method.isSynthetic();
    }
  };

  void bindTransactionInterceptors(final Binder binder);
}
