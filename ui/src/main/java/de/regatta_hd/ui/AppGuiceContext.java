package de.regatta_hd.ui;

import java.util.Collection;
import java.util.function.Supplier;

import com.gluonhq.ignite.guice.GuiceContext;
import com.google.inject.Module;

import javafx.fxml.FXMLLoader;

class AppGuiceContext extends GuiceContext implements FXMLLoaderFactory {

	AppGuiceContext(Object contextRoot, Supplier<Collection<Module>> modules) {
		super(contextRoot, modules);
	}

	@Override
	public FXMLLoader newLoader() {
		return super.injector.getInstance(FXMLLoader.class);
	}
}
