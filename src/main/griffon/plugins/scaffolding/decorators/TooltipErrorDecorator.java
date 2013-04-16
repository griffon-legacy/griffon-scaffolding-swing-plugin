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
import griffon.plugins.validation.FieldObjectError;
import griffon.plugins.validation.constraints.ConstrainedProperty;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.jdesktop.jxlayer.JXLayer;

import javax.swing.JComponent;
import java.awt.Graphics2D;
import java.util.List;

/**
 * @author Andres Almiray
 */
public class TooltipErrorDecorator extends AbstractErrorDecorator<JComponent> {
    private String toolTipText = null;

    @Override
    public void installUI(JComponent c) {
        toolTipText = c.getToolTipText();
    }

    @Override
    public void uninstallUI(JComponent c) {
        toolTipText = null;
    }

    public void paintLayerWithNoErrors(Graphics2D g2, JXLayer<? extends JComponent> layer, ScaffoldingContext scaffoldingContext, ConstrainedProperty constrainedProperty) {
        layer.getView().setToolTipText(toolTipText);
    }

    public void paintLayerWithErrors(Graphics2D g2, JXLayer<? extends JComponent> layer, ScaffoldingContext scaffoldingContext, ConstrainedProperty constrainedProperty) {
        List<FieldObjectError> fieldErrors = scaffoldingContext.getValidateable().getErrors().getFieldErrors(constrainedProperty.getPropertyName());
        String[] errorMessages = scaffoldingContext.resolveFieldErrorMessages(fieldErrors);
        layer.getView().setToolTipText(DefaultGroovyMethods.join(errorMessages, "\n"));
    }
}