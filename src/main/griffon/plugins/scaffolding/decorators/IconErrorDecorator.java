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
import griffon.util.GriffonClassUtils;
import org.jdesktop.jxlayer.JXLayer;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static griffon.plugins.scaffolding.ScaffoldingUtils.getUiDefaults;
import static griffon.util.ConfigUtils.getConfigValueAsString;
import static griffon.util.GriffonNameUtils.isBlank;

/**
 * @author Andres Almiray
 */
public class IconErrorDecorator extends AbstractErrorDecorator<JComponent> {
    public static final String KEY_ERRORS_DECORATORS_ICON_POSITION = "errors.decorators.icon.position";
    private static BufferedImage ACCEPT_ICON;
    private static BufferedImage CANCEL_ICON;

    private enum Position {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    static {
        try {
            ACCEPT_ICON = ImageIO.read(IconErrorDecorator.class.getResource("/griffon/plugins/scaffolding/decorators/accept.png"));
            CANCEL_ICON = ImageIO.read(IconErrorDecorator.class.getResource("/griffon/plugins/scaffolding/decorators/cancel.png"));
        } catch (IOException e) {
            // ignore?
        }
    }

    private Position position;

    public IconErrorDecorator() {
        String posStr = getConfigValueAsString(getUiDefaults(), KEY_ERRORS_DECORATORS_ICON_POSITION, Position.TOP_LEFT.name());
        try {
            position = Position.valueOf(posStr.toUpperCase().replace(" ", "_"));
        } catch (Exception e) {
            position = Position.TOP_LEFT;
        }
    }

    public void installUI(JComponent c) {
        JXLayer<JComponent> layer = (JXLayer<JComponent>) c;
        switch (position) {
            case TOP_LEFT:
            case BOTTOM_LEFT:
                layer.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
                break;
            case TOP_RIGHT:
            case BOTTOM_RIGHT:
                layer.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        }
    }

    public void uninstallUI(JComponent c) {
        JXLayer<JComponent> l = (JXLayer<JComponent>) c;
        l.setBorder(null);
    }

    public void paintLayerWithNoErrors(Graphics2D g2, JXLayer<? extends JComponent> layer, ScaffoldingContext scaffoldingContext, ConstrainedProperty constrainedProperty) {
        try {
            Object propertyValue = GriffonClassUtils.getProperty(scaffoldingContext.getValidateable(), constrainedProperty.getPropertyName());
            if (propertyValue == null || propertyValue instanceof CharSequence && isBlank(String.valueOf(propertyValue))) {
                layer.getParent().repaint();
            } else {
                switch (position) {
                    case TOP_LEFT:
                        g2.drawImage(ACCEPT_ICON, 0, 0, null);
                        break;
                    case TOP_RIGHT:
                        g2.drawImage(ACCEPT_ICON, layer.getWidth() - ACCEPT_ICON.getWidth() - 1, 0, null);
                        break;
                    case BOTTOM_LEFT:
                        g2.drawImage(ACCEPT_ICON, 0, layer.getHeight() - ACCEPT_ICON.getHeight() - 1, null);
                        break;
                    case BOTTOM_RIGHT:
                        g2.drawImage(ACCEPT_ICON, layer.getWidth() - ACCEPT_ICON.getWidth() - 1, layer.getHeight() - ACCEPT_ICON.getHeight() - 1, null);
                }
            }
        } catch (IllegalAccessException e) {
            // ignore
        } catch (InvocationTargetException e) {
            // ignore
        } catch (NoSuchMethodException e) {
            // ignore
        }
    }

    public void paintLayerWithErrors(Graphics2D g2, JXLayer<? extends JComponent> layer, ScaffoldingContext scaffoldingContext, ConstrainedProperty constrainedProperty) {
        switch (position) {
            case TOP_LEFT:
                g2.drawImage(CANCEL_ICON, 0, 0, null);
                break;
            case TOP_RIGHT:
                g2.drawImage(CANCEL_ICON, layer.getWidth() - CANCEL_ICON.getWidth() - 1, 0, null);
                break;
            case BOTTOM_LEFT:
                g2.drawImage(CANCEL_ICON, 0, layer.getHeight() - CANCEL_ICON.getHeight() - 1, null);
                break;
            case BOTTOM_RIGHT:
                g2.drawImage(CANCEL_ICON, layer.getWidth() - CANCEL_ICON.getWidth() - 1, layer.getHeight() - CANCEL_ICON.getHeight() - 1, null);
        }
    }
}