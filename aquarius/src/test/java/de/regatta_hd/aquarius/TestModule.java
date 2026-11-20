package de.regatta_hd.aquarius;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import de.regatta_hd.commons.core.ConfigService;
import java.io.IOException;

class TestModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ConfigService.class).to(TestConfigService.class);

    try {
      bind(ConfigService.class)
          .annotatedWith(Names.named("default"))
          .toInstance(ConfigService.createDefault());
    } catch (IOException e) {
      addError(e);
    }
  }
}
