package org.mybatis.guice.datasource.builtin;

import com.google.inject.Inject;

import javax.inject.Named;

final class BuiltinDataSourceProviderArguments {
  @Inject @Named("JDBC.driver") String driver;
  @Inject @Named("JDBC.url") String url;
  @Inject(optional = true) @Named("JDBC.driverClassLoader") ClassLoader driverClassLoader;
}
