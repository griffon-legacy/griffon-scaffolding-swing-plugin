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

package griffon.plugins.scaffolding;

import griffon.builder.css.CssClass;
import griffon.plugins.validation.constraints.ConstrainedProperty;
import groovy.lang.MissingPropertyException;

import javax.swing.JComponent;
import java.awt.Component;
import java.util.Map;

import static com.feature50.clarity.ClarityConstants.CLIENT_PROPERTY_CLASS_KEY;
import static griffon.util.GriffonNameUtils.isBlank;

/**
 * @author Andres Almiray
 */
public final class SwingScaffoldingContext extends ScaffoldingContext {
    public SwingScaffoldingContext() {

    }

    @Override
    public Map<String, Object> widgetAttributes(String widget, ConstrainedProperty constrainedProperty) {
        Map<String, Object> attributes = super.widgetAttributes(widget, constrainedProperty);
        attributes.put("enabled", constrainedProperty.isEnabled());
        return attributes;
    }

    public void bind(Component source, String propertyName, AtomicValue property, ConstrainedProperty constrainedProperty) {
        addDisposable(PropertyBinding.create(source, propertyName, property, constrainedProperty));
    }

    public void applyCssOnError(String... cssclasses) {
        if (cssclasses == null || cssclasses.length == 0) return;
        for (Map.Entry<String, ConstrainedProperty> property : getValidateable().constrainedProperties().entrySet()) {
            String propertyName = property.getKey();
            ConstrainedProperty constrainedProperty = property.getValue();
            if (!constrainedProperty.isDisplay()) continue;
            boolean hasErrors = getValidateable().getErrors().getFieldErrorCount(propertyName) != 0;
            try {
                JComponent labeler = (JComponent) getBinding().getVariable(propertyName + "_labeler");
                if (labeler != null) {
                    CssClass cssClass = cssClassOf(labeler);
                    if (hasErrors) {
                        cssClass.addAll(cssclasses);
                    } else {
                        cssClass.removeAll(cssclasses);
                    }
                }
                JComponent editor = (JComponent) getBinding().getVariable(propertyName);
                if (editor != null) {
                    CssClass cssClass = cssClassOf(labeler);
                    if (hasErrors) {
                        cssClass.addAll(cssclasses);
                    } else {
                        cssClass.removeAll(cssclasses);
                    }
                }
            } catch (MissingPropertyException mpe) {
                // ignore
            }
        }
    }

    public void addCss(String... cssclasses) {
        if (cssclasses == null || cssclasses.length == 0) return;
        for (Map.Entry<String, ConstrainedProperty> property : getValidateable().constrainedProperties().entrySet()) {
            String propertyName = property.getKey();
            ConstrainedProperty constrainedProperty = property.getValue();
            if (!constrainedProperty.isDisplay()) continue;
            try {
                JComponent labeler = (JComponent) getBinding().getVariable(propertyName + "_labeler");
                if (labeler != null) {
                    CssClass cssClass = cssClassOf(labeler);
                    cssClass.addAll(cssclasses);
                }
                JComponent editor = (JComponent) getBinding().getVariable(propertyName);
                if (editor != null) {
                    CssClass cssClass = cssClassOf(labeler);
                    cssClass.addAll(cssclasses);
                }
            } catch (MissingPropertyException mpe) {
                // ignore
            }
        }
    }

    public void removeCss(String... cssclasses) {
        if (cssclasses == null || cssclasses.length == 0) return;
        for (Map.Entry<String, ConstrainedProperty> property : getValidateable().constrainedProperties().entrySet()) {
            String propertyName = property.getKey();
            ConstrainedProperty constrainedProperty = property.getValue();
            if (!constrainedProperty.isDisplay()) continue;
            try {
                JComponent labeler = (JComponent) getBinding().getVariable(propertyName + "_labeler");
                if (labeler != null) {
                    CssClass cssClass = cssClassOf(labeler);
                    cssClass.removeAll(cssclasses);
                }
                JComponent editor = (JComponent) getBinding().getVariable(propertyName);
                if (editor != null) {
                    CssClass cssClass = cssClassOf(labeler);
                    cssClass.removeAll(cssclasses);
                }
            } catch (MissingPropertyException mpe) {
                // ignore
            }
        }
    }

    public void toggleCss(String cssclass) {
        if (isBlank(cssclass)) return;
        for (Map.Entry<String, ConstrainedProperty> property : getValidateable().constrainedProperties().entrySet()) {
            String propertyName = property.getKey();
            ConstrainedProperty constrainedProperty = property.getValue();
            if (!constrainedProperty.isDisplay()) continue;
            try {
                JComponent labeler = (JComponent) getBinding().getVariable(propertyName + "_labeler");
                if (labeler != null) {
                    CssClass cssClass = cssClassOf(labeler);
                    cssClass.toggle(cssclass);
                }
                JComponent editor = (JComponent) getBinding().getVariable(propertyName);
                if (editor != null) {
                    CssClass cssClass = cssClassOf(labeler);
                    cssClass.toggle(cssclass);
                }
            } catch (MissingPropertyException mpe) {
                // ignore
            }
        }
    }

    private CssClass cssClassOf(JComponent component) {
        Object clientProperty = component.getClientProperty(CLIENT_PROPERTY_CLASS_KEY);
        if (clientProperty == null) {
            clientProperty = new CssClass();
            component.putClientProperty(CLIENT_PROPERTY_CLASS_KEY, clientProperty);
        }
        return clientProperty instanceof CssClass ? (CssClass) clientProperty : null;
    }
}
