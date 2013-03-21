/*
 * Copyright 2009-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package griffon.plugins.scaffolding.decorators;

import griffon.plugins.scaffolding.ScaffoldingContext;
import griffon.plugins.validation.constraints.ConstrainedProperty;
import org.jdesktop.jxlayer.JXLayer;

import javax.swing.JComponent;
import java.awt.Graphics2D;

/**
 * @author Andres Almiray
 */
public interface ErrorDecorator<V extends JComponent> {
    void paintLayerWithErrors(Graphics2D g2, JXLayer<? extends V> layer, ScaffoldingContext scaffoldingContext, ConstrainedProperty constrainedProperty);

    void paintLayerWithNoErrors(Graphics2D g2, JXLayer<? extends V> layer, ScaffoldingContext scaffoldingContext, ConstrainedProperty constrainedProperty);

    void installUI(JComponent c);

    void uninstallUI(JComponent c);
}
