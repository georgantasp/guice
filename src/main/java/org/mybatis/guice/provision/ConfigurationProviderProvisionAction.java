package org.mybatis.guice.provision;

import org.mybatis.guice.configuration.ConfigurationProvider;

public interface ConfigurationProviderProvisionAction {
  void perform(ConfigurationProvider configurationProvider);
}
