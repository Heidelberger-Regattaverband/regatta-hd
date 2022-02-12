package de.regatta_hd.ui;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Supplier;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;

import de.regatta_hd.ui.util.DBTaskRunner;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;

/**
 * Implementation of dependency injection context for Guice
 */
public class GuiceContext implements FXMLLoaderFactory {

	private final Application contextRoot;

	private Injector injector;

	private final Supplier<Collection<Module>> modules;

	/**
	 * Create the Guice context
	 *
	 * @param contextRoot root object to inject
	 * @param modules     custom Guice modules
	 */
	public GuiceContext(Application contextRoot, Supplier<Collection<Module>> modules) {
		this.contextRoot = Objects.requireNonNull(contextRoot);
		this.modules = Objects.requireNonNull(modules);
	}

	@Override
	public final FXMLLoader newFXMLLoader() {
		return this.injector.getInstance(FXMLLoader.class);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void init() {
		Collection<Module> uniqueModules = new HashSet<>(this.modules.get());
		uniqueModules.add(new FXModule());
		this.injector = Guice.createInjector(uniqueModules);
		this.injector.injectMembers(this.contextRoot);
	}

	private class FXModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(FXMLLoaderFactory.class).toInstance(GuiceContext.this);
			bind(DBTaskRunner.class);
		}

		@Provides
		FXMLLoader provideFxmlLoader() {
			FXMLLoader loader = new FXMLLoader();
			loader.setControllerFactory(GuiceContext.this.injector::getInstance);
			return loader;
		}
	}
}
