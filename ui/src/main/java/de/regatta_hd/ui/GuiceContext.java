/*
 * Copyright (c) 2020, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
	public FXMLLoader newLoader() {
		return this.injector.getInstance(FXMLLoader.class);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void init() {
		Collection<Module> uniqueModules = new HashSet<>(this.modules.get());
		uniqueModules.add(new FXModule());
		this.injector = Guice.createInjector(uniqueModules.toArray(new Module[0]));
		this.injector.injectMembers(this.contextRoot);
	}

	private class FXModule extends AbstractModule {

		@Override
		protected void configure() {
		}

		@Provides
		FXMLLoader provideFxmlLoader() {
			FXMLLoader loader = new FXMLLoader();
			loader.setControllerFactory(GuiceContext.this.injector::getInstance);
			return loader;
		}
	}
}
