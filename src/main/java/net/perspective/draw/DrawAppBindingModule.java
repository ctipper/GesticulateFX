/**
 * DrawAppBindingModule.java
 * 
 * Created on 25 Sept 2025 15:22:47
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

import javax.inject.Singleton;
import dagger.Binds;
import dagger.Module;
import net.perspective.draw.geom.FigureFactory;
import net.perspective.draw.geom.FigureFactoryImpl;
import net.perspective.draw.text.Editor;
import net.perspective.draw.text.TextEditor;

/**
 *
 * @author ctipper
 */

@Module
public abstract class DrawAppBindingModule {

    @Binds
    @Singleton
    abstract FigureFactory bindFigureFactory(FigureFactoryImpl impl);

    @Binds
    @Singleton
    abstract Editor bindEditor(TextEditor textEditor);

}
