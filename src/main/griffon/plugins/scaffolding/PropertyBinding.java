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

import griffon.core.resources.editors.PropertyEditorResolver;
import griffon.exceptions.GriffonException;
import griffon.plugins.validation.constraints.ConstrainedProperty;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableModel;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;

import static griffon.util.GriffonClassUtils.getProperty;
import static griffon.util.GriffonClassUtils.setProperty;

/**
 * @author Andres Almiray
 */
public class PropertyBinding extends AbstractPropertyBinding {
    private static final String VALUE = "value";
    private Component source;
    private String sourcePropertyName;
    private AtomicValue property;
    private PropertyDescriptor sourcePropertyDescriptor;

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
        super(constrainedProperty);
        this.source = source;
        this.sourcePropertyName = sourcePropertyName;
        this.property = property;
        this.sourcePropertyDescriptor = resolvePropertyDescriptor(source, sourcePropertyName);

        bind();
    }

    public void dispose() {
        source.removePropertyChangeListener(sourcePropertyName, sourceChangeListener);
        property.removePropertyChangeListener(VALUE, targetChangeListener);
        source = null;
        sourcePropertyDescriptor = null;
        property = null;
        super.dispose();
    }

    protected void bindSource() {
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
            }
        } else if (source instanceof AbstractButton) {
            AbstractButton buttonComponent = (AbstractButton) source;
            if ("selected".equals(sourcePropertyName)) {
                final ItemListener itemListener = new ItemListener() {
                    public void itemStateChanged(ItemEvent itemEvent) {
                        updateTarget();
                    }
                };
                buttonComponent.addPropertyChangeListener("model", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        updateTarget();
                        ((ButtonModel) event.getOldValue()).removeItemListener(itemListener);
                        ((ButtonModel) event.getNewValue()).addItemListener(itemListener);
                    }
                });
                buttonComponent.getModel().addItemListener(itemListener);
            }
        } else if (source instanceof JComboBox) {
            JComboBox comboBoxComponent = (JComboBox) source;
            if ("selectedElement".equals(sourcePropertyName) ||
                "selectedItem".equals(sourcePropertyName) ||
                "selectedIndex".equals(sourcePropertyName)) {
                final ItemListener itemListener = new ItemListener() {
                    public void itemStateChanged(ItemEvent itemEvent) {
                        updateTarget();
                    }
                };
                comboBoxComponent.addPropertyChangeListener("model", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        updateTarget();
                    }
                });
                comboBoxComponent.addItemListener(itemListener);
            } else if ("elements".equals(sourcePropertyName)) {
                final ListDataListener listDataListener = new ListDataListener() {
                    public void intervalAdded(ListDataEvent listDataEvent) {
                        updateTarget();
                    }

                    public void intervalRemoved(ListDataEvent listDataEvent) {
                        updateTarget();
                    }

                    public void contentsChanged(ListDataEvent listDataEvent) {
                        updateTarget();
                    }
                };
                comboBoxComponent.addPropertyChangeListener("model", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        updateTarget();
                        ((ComboBoxModel) event.getOldValue()).removeListDataListener(listDataListener);
                        ((ComboBoxModel) event.getNewValue()).addListDataListener(listDataListener);
                    }
                });
                comboBoxComponent.getModel().addListDataListener(listDataListener);
            }
        } else if (source instanceof JList) {
            JList listComponent = (JList) source;
            if ("selectedValue".equals(sourcePropertyName) ||
                "selectedElement".equals(sourcePropertyName) ||
                "selectedValues".equals(sourcePropertyName) ||
                "selectedElements".equals(sourcePropertyName) ||
                "selectedIndex".equals(sourcePropertyName) ||
                "selectedIndices".equals(sourcePropertyName)) {
                final ListSelectionListener listSelectionListener = new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent listSelectionEvent) {
                        updateTarget();
                    }
                };
                listComponent.addPropertyChangeListener("model", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        updateTarget();
                        ((ListSelectionModel) event.getOldValue()).removeListSelectionListener(listSelectionListener);
                        ((ListSelectionModel) event.getNewValue()).addListSelectionListener(listSelectionListener);
                    }
                });
                listComponent.addListSelectionListener(listSelectionListener);
            } else if ("elements".equals(sourcePropertyName)) {
                final ListDataListener listDataListener = new ListDataListener() {
                    public void intervalAdded(ListDataEvent listDataEvent) {
                        updateTarget();
                    }

                    public void intervalRemoved(ListDataEvent listDataEvent) {
                        updateTarget();
                    }

                    public void contentsChanged(ListDataEvent listDataEvent) {
                        updateTarget();
                    }
                };
                listComponent.addPropertyChangeListener("model", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        updateTarget();
                        ((ListModel) event.getOldValue()).removeListDataListener(listDataListener);
                        ((ListModel) event.getNewValue()).addListDataListener(listDataListener);
                    }
                });
                listComponent.getModel().addListDataListener(listDataListener);
            }
        } else if (source instanceof JSlider) {
            JSlider sliderComponent = (JSlider) source;
            if ("value".equals(sourcePropertyName)) {
                final ChangeListener changeListener = new ChangeListener() {
                    public void stateChanged(ChangeEvent changeEvent) {
                        updateTarget();
                    }
                };
                sliderComponent.addPropertyChangeListener("model", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        updateTarget();
                        ((BoundedRangeModel) event.getOldValue()).removeChangeListener(changeListener);
                        ((BoundedRangeModel) event.getNewValue()).addChangeListener(changeListener);
                    }
                });
                sliderComponent.getModel().addChangeListener(changeListener);
            }
        } else if (source instanceof JSpinner) {
            JSpinner spinnerComponent = (JSpinner) source;
            if ("value".equals(sourcePropertyName)) {
                final ChangeListener changeListener = new ChangeListener() {
                    public void stateChanged(ChangeEvent changeEvent) {
                        updateTarget();
                    }
                };
                spinnerComponent.addPropertyChangeListener("model", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        updateTarget();
                        ((SpinnerModel) event.getOldValue()).removeChangeListener(changeListener);
                        ((SpinnerModel) event.getNewValue()).addChangeListener(changeListener);
                    }
                });
                spinnerComponent.getModel().addChangeListener(changeListener);
            }
        } else if (source instanceof JTable) {
            JTable tableComponent = (JTable) source;
            if ("selectedElement".equals(sourcePropertyName) ||
                "selectedElements".equals(sourcePropertyName)) {
                final ListSelectionListener listSelectionListener = new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent listSelectionEvent) {
                        updateTarget();
                    }
                };
                tableComponent.addPropertyChangeListener("model", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        updateTarget();
                        ((ListSelectionModel) event.getOldValue()).removeListSelectionListener(listSelectionListener);
                        ((ListSelectionModel) event.getNewValue()).addListSelectionListener(listSelectionListener);
                    }
                });
                tableComponent.getSelectionModel().addListSelectionListener(listSelectionListener);
            } else if ("elements".equals(sourcePropertyName)) {
                final TableModelListener tableModelListener = new TableModelListener() {
                    public void tableChanged(TableModelEvent tableModelEvent) {
                        updateTarget();
                    }
                };
                tableComponent.addPropertyChangeListener("model", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        updateTarget();
                        ((TableModel) event.getOldValue()).removeTableModelListener(tableModelListener);
                        ((TableModel) event.getNewValue()).addTableModelListener(tableModelListener);
                    }
                });
                tableComponent.getModel().addTableModelListener(tableModelListener);
            }
        }
        source.addPropertyChangeListener(sourcePropertyName, sourceChangeListener);
    }

    protected void bindTarget() {
        property.addPropertyChangeListener(VALUE, targetChangeListener);
    }

    protected Object getTargetPropertyValue() {
        return property.getValue();
    }

    protected void setTargetPropertyValue(Object value) {
        property.setValue(value);
    }

    protected void setSourcePropertyValue(Object value) {
        String propertyName = sourcePropertyName;

        if (source instanceof JComboBox) {
            if ("selectedElement".equals(sourcePropertyName)) {
                propertyName = "selectedItem";
            }
        }

        try {
            setProperty(source, propertyName, value);
        } catch (IllegalAccessException e) {
            throw new GriffonException(e);
        } catch (InvocationTargetException e) {
            throw new GriffonException(e);
        } catch (NoSuchMethodException e) {
            throw new GriffonException(e);
        }
    }

    protected Object getSourcePropertyValue() {
        String propertyName = sourcePropertyName;

        if (source instanceof JComboBox) {
            if ("selectedElement".equals(sourcePropertyName)) {
                propertyName = "selectedItem";
            }
        }

        try {
            return getProperty(source, propertyName);
        } catch (IllegalAccessException e) {
            throw new GriffonException(e);
        } catch (InvocationTargetException e) {
            throw new GriffonException(e);
        } catch (NoSuchMethodException e) {
            throw new GriffonException(e);
        }
    }

    protected PropertyEditor resolveSourcePropertyEditor() {
        return PropertyEditorResolver.findEditor(sourcePropertyDescriptor.getPropertyType());
    }
}
