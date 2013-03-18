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

import griffon.core.resources.editors.ExtendedPropertyEditor;
import griffon.core.resources.editors.ValueConversionException;
import griffon.exceptions.GriffonException;
import griffon.plugins.validation.constraints.ConstrainedProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;

import static griffon.util.GriffonClassUtils.*;
import static griffon.util.GriffonExceptionHandler.sanitize;

/**
 * @author Andres Almiray
 */
public class PropertyBinding implements Disposable {
    private static final Logger LOG = LoggerFactory.getLogger(PropertyBinding.class);

    private static final String VALUE = "value";
    private Component source;
    private String sourcePropertyName;
    private AtomicValue property;
    private ConstrainedProperty constrainedProperty;
    private PropertyDescriptor sourcePropertyDescriptor;
    private final Object LOCK = new Object[0];
    private boolean firing = false;

    private final PropertyChangeListener sourceChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            updateTarget();
        }
    };

    private final PropertyChangeListener targetChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            updateSource();
        }
    };

    public static PropertyBinding create(final Component source, final String sourcePropertyName, final AtomicValue property, ConstrainedProperty constrainedProperty) {
        return new PropertyBinding(source, sourcePropertyName, property, constrainedProperty);
    }

    private PropertyBinding(final Component source, final String sourcePropertyName, AtomicValue property, ConstrainedProperty constrainedProperty) {
        this.source = source;
        this.sourcePropertyName = sourcePropertyName;
        this.constrainedProperty = constrainedProperty;
        this.property = property;
        this.sourcePropertyDescriptor = resolvePropertyDescriptor(source, sourcePropertyName);

        bindSource();
        bindTarget();

        if (getTargetPropertyValue() != null) {
            updateSource();
        } else {
            updateTarget();
        }
    }

    public void dispose() {
        source.removePropertyChangeListener(sourcePropertyName, sourceChangeListener);
        property.removePropertyChangeListener(VALUE, targetChangeListener);
        source = null;
        sourcePropertyDescriptor = null;
        property = null;
        constrainedProperty = null;
    }

    private void updateSource() {
        synchronized (LOCK) {
            if (firing) return;
            firing = true;
            try {
                PropertyEditor editor = resolveSourcePropertyEditor();
                editor.setValue(getTargetPropertyValue());
                setSourcePropertyValue(editor.getValue());
            } catch (ValueConversionException e) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Could not update target property '" + constrainedProperty.getPropertyName() + "'", sanitize(e));
                }
            } finally {
                firing = false;
            }
        }
    }

    private void updateTarget() {
        synchronized (LOCK) {
            if (firing) return;
            firing = true;
            try {
                PropertyEditor editor = resolveTargetPropertyEditor();
                editor.setValue(getSourcePropertyValue());
                setTargetPropertyValue(editor.getValue());
            } catch (ValueConversionException e) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Could not update source property '" + sourcePropertyName + "'", sanitize(e));
                }
                setTargetPropertyValue(null);
            } finally {
                firing = false;
            }
        }
    }

    private void bindSource() {
        if (source instanceof JTextComponent) {
            JTextComponent textComponent = (JTextComponent) source;
            if ("text".equals(sourcePropertyName)) {
                final DocumentListener documentListener = new DocumentListener() {
                    public void insertUpdate(DocumentEvent documentEvent) {
                        updateTarget();
                    }

                    public void removeUpdate(DocumentEvent documentEvent) {
                        updateTarget();
                    }

                    public void changedUpdate(DocumentEvent documentEvent) {
                        updateTarget();
                    }
                };
                textComponent.addPropertyChangeListener("document", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        updateTarget();
                        ((Document) event.getOldValue()).removeDocumentListener(documentListener);
                        ((Document) event.getNewValue()).addDocumentListener(documentListener);
                    }
                });
                textComponent.getDocument().addDocumentListener(documentListener);
            } else {
                source.addPropertyChangeListener(sourcePropertyName, sourceChangeListener);
            }
        } else {
            source.addPropertyChangeListener(sourcePropertyName, sourceChangeListener);
        }
    }

    private void bindTarget() {
        property.addPropertyChangeListener(VALUE, targetChangeListener);
    }

    private Object getTargetPropertyValue() {
        return property.getValue();
    }

    private void setTargetPropertyValue(Object value) {
        property.setValue(value);
    }

    private void setSourcePropertyValue(Object value) {
        try {
            setProperty(source, sourcePropertyName, value);
        } catch (IllegalAccessException e) {
            throw new GriffonException(e);
        } catch (InvocationTargetException e) {
            throw new GriffonException(e);
        } catch (NoSuchMethodException e) {
            throw new GriffonException(e);
        }
    }

    private Object getSourcePropertyValue() {
        try {
            return getProperty(source, sourcePropertyName);
        } catch (IllegalAccessException e) {
            throw new GriffonException(e);
        } catch (InvocationTargetException e) {
            throw new GriffonException(e);
        } catch (NoSuchMethodException e) {
            throw new GriffonException(e);
        }
    }

    private PropertyEditor resolveTargetPropertyEditor() {
        PropertyEditor editor = PropertyEditorManager.findEditor(constrainedProperty.getPropertyType());
        if (editor instanceof ExtendedPropertyEditor) {
            ((ExtendedPropertyEditor) editor).setFormat(constrainedProperty.getFormat());
        }
        return editor;
    }

    private PropertyEditor resolveSourcePropertyEditor() {
        PropertyEditor editor = PropertyEditorManager.findEditor(sourcePropertyDescriptor.getPropertyType());
        if (editor instanceof ExtendedPropertyEditor) {
            ((ExtendedPropertyEditor) editor).setFormat(constrainedProperty.getFormat());
        }
        return editor;
    }

    private PropertyDescriptor resolvePropertyDescriptor(Object source, String sourcePropertyName) {
        try {
            return getPropertyDescriptor(source, sourcePropertyName);
        } catch (IllegalAccessException e) {
            throw new GriffonException(e);
        } catch (InvocationTargetException e) {
            throw new GriffonException(e.getTargetException());
        } catch (NoSuchMethodException e) {
            throw new GriffonException(e);
        }
    }
}
