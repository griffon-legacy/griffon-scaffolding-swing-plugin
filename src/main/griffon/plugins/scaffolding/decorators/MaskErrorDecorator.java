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

import griffon.core.resources.formatters.ParseException;
import griffon.plugins.scaffolding.ScaffoldingContext;
import griffon.plugins.validation.constraints.ConstrainedProperty;
import griffon.swing.formatters.ColorFormatter;
import org.jdesktop.jxlayer.JXLayer;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import java.awt.*;

import static griffon.plugins.scaffolding.ScaffoldingUtils.getUiDefaults;
import static griffon.util.ConfigUtils.getConfigValueAsString;

/**
 * @author Andres Almiray
 */
public class MaskErrorDecorator extends AbstractErrorDecorator<JTextComponent> {
    private static final String KEY_ERRORS_DECORATORS_MASK_COLOR = "errors.decorators.mask.color";
    private Color color = Color.RED;

    @Override
    public void installUI(JComponent c) {
        String colorStr = getConfigValueAsString(getUiDefaults(), KEY_ERRORS_DECORATORS_MASK_COLOR, "#F00");
        try {
            color = ColorFormatter.parseColor(colorStr);
        } catch (ParseException pe) {
            color = Color.RED;
        }
    }

    @Override
    public void uninstallUI(JComponent c) {
        color = null;
    }

    public void paintLayerWithErrors(Graphics2D g2, JXLayer<? extends JTextComponent> layer, ScaffoldingContext scaffoldingContext, ConstrainedProperty constrainedProperty) {
        // to be in sync with the view if the layer has a border
        Insets layerInsets = layer.getInsets();
        g2.translate(layerInsets.left, layerInsets.top);

        JTextComponent view = layer.getView();
        // To prevent painting on view's border
        Insets insets = view.getInsets();
        g2.clip(new Rectangle(insets.left, insets.top,
            view.getWidth() - insets.left - insets.right,
            view.getHeight() - insets.top - insets.bottom));

        g2.setColor(color);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .2f));
        g2.fillRect(0, 0, layer.getWidth(), layer.getHeight());
    }
}