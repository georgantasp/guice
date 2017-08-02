package org.mybatis.guice.provision;

import com.google.inject.spi.ProvisionListener;

import org.mybatis.guice.environment.EnvironmentProvider;

public class EnvironmentProviderProvisionListener implements ProvisionListener {

  private final ProvisionAction<EnvironmentProvider> action;

  EnvironmentProviderProvisionListener(ProvisionAction<EnvironmentProvider> action) {
    this.action = action;
  }
  
  @Override
  public <T> void onProvision(ProvisionInvocation<T> provision) {
    EnvironmentProvider configurationProvider = (EnvironmentProvider) provision.provision();
    this.action.perform(configurationProvider);
  }

  public static EnvironmentProviderProvisionListener create(final ProvisionAction<EnvironmentProvider> action) {
    return new EnvironmentProviderProvisionListener(action);
  }
}
