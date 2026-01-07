/**
 * FxAppComponent.java
 * 
 * Created on 24 Sept 2025 08:56:16
 * 
 */

/**
 * Copyright (c) 2026 Christopher Tipper
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

import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.inject.Provider;
import dagger.BindsInstance;
import dagger.Subcomponent;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

/**
 *
 * @author overheadhunter
 */

@FxAppScoped
@Subcomponent(modules = {FxAppModule.class})
interface FxAppComponent {

    Function<URL, FXMLLoader> fxmlLoaderFactory();

    Map<Class<?>, Provider<Object>> controllers();

    default FXMLLoader loader(URL fxmlUrl) {
        return fxmlLoaderFactory().apply(Objects.requireNonNull(fxmlUrl));
    }

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        @BindsInstance
        Builder mainWindow(Stage mainWindow);

        FxAppComponent build();
    }

}
