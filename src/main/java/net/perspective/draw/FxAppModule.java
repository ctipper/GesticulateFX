/**
 * FxAppModule.java
 *
 * Created on 24 Sept 2025 08:56:16
 *
 */

/**
 * Copyright (c) 2025 e-conomist
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.perspective.draw;

import java.io.*;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Function;
import javax.inject.Provider;
import dagger.Module;
import dagger.Provides;
import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadException;
import javafx.util.Callback;

/**
 *
 * @author overheadhunter
 */

@Module(includes = {FxControllersModule.class})
class FxAppModule {

    @Provides
    @FxAppScoped
    ResourceBundle provideResourceBundle() {
        try {
            return ResourceBundle.getBundle("ui");
        } catch (MissingResourceException e) {
            return ResourceBundle.getBundle("ui", Locale.ENGLISH);
        }
    }

    @Provides
    @FxAppScoped
    Callback<Class<?>, Object> provideControllerFactory(Map<Class<?>, Provider<Object>> providers) {
        return clazz -> {
            Provider<?> provider = providers.get(clazz);
            if (provider == null) {
                throw new UncheckedIOException(new LoadException("Controller not registered for class: " + clazz.getName()));
            }
            Object controller = provider.get();
            if (!clazz.isInstance(controller)) {
                throw new UncheckedIOException(new LoadException("Registered controller not instance of class: " + clazz.getName()));
            }
            return controller;
        };
    }

    @Provides
    @FxAppScoped
    Function<URL, FXMLLoader> provideFxmlLoaderFactory(Callback<Class<?>, Object> controllerFactory, ResourceBundle resourceBundle) {
        return url -> {
            FXMLLoader loader = new FXMLLoader(url, resourceBundle);
            loader.setControllerFactory(controllerFactory);
            return loader;
        };
    }
}
