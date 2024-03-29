package de.regatta_hd.commons.fx.guice;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Supplier;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;

/**
 * Implementation of dependency injection context for Guice
 */
public class GuiceContext implements FXMLLoaderFactory {

	private final Application contextRoot;

	private final Supplier<Collection<Module>> modules;

	private Injector injector;

	/**
	 * Create the Guice context
	 *
	 * @param contextRoot root object to inject
	 * @param modules     custom Guice modules
	 */
	public GuiceContext(Application contextRoot, Supplier<Collection<Module>> modules) {
		this.contextRoot = requireNonNull(contextRoot);
		this.modules = requireNonNull(modules);
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
		}

		@Provides
		FXMLLoader provideFxmlLoader() {
			FXMLLoader loader = new FXMLLoader();
			loader.setControllerFactory(GuiceContext.this.injector::getInstance);
			return loader;
		}
	}
}
