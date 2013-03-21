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

package griffon.plugins.scaffolding.nodes;

import griffon.plugins.scaffolding.ScaffoldingContext;
import griffon.plugins.scaffolding.decorators.ErrorDecorator;
import griffon.plugins.validation.constraints.ConstrainedProperty;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;

import javax.swing.JComponent;
import java.awt.Graphics2D;
import java.util.List;

/**
 * @author Andres Almiray
 */
public class CompositeLayerUI<V extends JComponent> extends AbstractLayerUI<V> {
    private final ScaffoldingContext scaffoldingContext;
    private final ConstrainedProperty constrainedProperty;
    private final ErrorDecorator<V>[] decorators;

    public CompositeLayerUI(ScaffoldingContext scaffoldingContext, ConstrainedProperty constrainedProperty, ErrorDecorator<V>... decorators) {
        this.scaffoldingContext = scaffoldingContext;
        this.constrainedProperty = constrainedProperty;
        this.decorators = decorators;
    }

    public CompositeLayerUI(ScaffoldingContext scaffoldingContext, ConstrainedProperty constrainedProperty, List<ErrorDecorator<V>> decorators) {
        this.scaffoldingContext = scaffoldingContext;
        this.constrainedProperty = constrainedProperty;
        this.decorators = decorators.toArray(new ErrorDecorator[decorators.size()]);
    }

    @Override
    public void installUI(JComponent component) {
        for (ErrorDecorator decorator : decorators) {
            decorator.installUI(component);
        }
        super.installUI(component);
    }

    @Override
    public void uninstallUI(JComponent component) {
        for (ErrorDecorator decorator : decorators) {
            decorator.uninstallUI(component);
        }
        super.uninstallUI(component);
    }

    protected boolean hasErrors(JXLayer<? extends JComponent> layer) {
        String propertyName = constrainedProperty.getPropertyName();
        return scaffoldingContext.getValidateable().getErrors().getFieldErrorCount(propertyName) != 0;
    }

    @Override
    protected void paintLayer(Graphics2D g2, JXLayer<? extends V> layer) {
        super.paintLayer(g2, layer);
        if (hasErrors(layer)) {
            for (ErrorDecorator decorator : decorators) {
                decorator.paintLayerWithErrors(g2, layer, scaffoldingContext, constrainedProperty);
            }
        } else {
            for (ErrorDecorator decorator : decorators) {
                decorator.paintLayerWithNoErrors(g2, layer, scaffoldingContext, constrainedProperty);
            }
        }
    }
}
