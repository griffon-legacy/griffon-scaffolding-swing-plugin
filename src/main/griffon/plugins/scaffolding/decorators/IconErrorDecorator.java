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

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * @author Andres Almiray
 */
public class IconErrorDecorator extends AbstractErrorDecorator<JTextComponent> {
    // The red icon to be shown at the layer's corner
    private final static BufferedImage INVALID_ICON;

    static {
        int width = 7;
        int height = 8;
        INVALID_ICON = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) INVALID_ICON.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setColor(Color.RED);
        g2.fillRect(0, 0, width, height);
        g2.setColor(Color.WHITE);
        g2.drawLine(0, 0, width, height);
        g2.drawLine(0, height, width, 0);
        g2.dispose();
    }

    public void installUI(JComponent c) {
        JXLayer<JTextComponent> l = (JXLayer<JTextComponent>) c;
        // set necessary insets for the layer
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
    }

    public void uninstallUI(JComponent c) {
        JXLayer<JTextComponent> l = (JXLayer<JTextComponent>) c;
        l.setBorder(null);
    }

    public void paintLayerWithErrors(Graphics2D g2, JXLayer<? extends JTextComponent> layer, ScaffoldingContext scaffoldingContext, ConstrainedProperty constrainedProperty) {
        g2.drawImage(INVALID_ICON, layer.getWidth() - INVALID_ICON.getWidth() - 1, layer.getHeight() - INVALID_ICON.getHeight() - 1, null);
    }
}