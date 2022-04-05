package de.regatta_hd.commons.fx;

import com.google.inject.AbstractModule;

import de.regatta_hd.commons.fx.db.DBTaskRunner;
import de.regatta_hd.commons.fx.impl.DBTaskRunnerImpl;
import de.regatta_hd.commons.fx.impl.WindowManagerImpl;
import de.regatta_hd.commons.fx.stage.WindowManager;

public class CommonsFXModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(DBTaskRunner.class).to(DBTaskRunnerImpl.class);
		bind(WindowManager.class).to(WindowManagerImpl.class);
	}
}
