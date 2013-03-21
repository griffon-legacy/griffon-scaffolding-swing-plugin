package griffon.plugins.scaffolding.templates

import griffon.plugins.scaffolding.atoms.EnumValue

import javax.swing.DefaultComboBoxModel

Map widgetAttributes = scaffoldingContext.widgetAttributes('comboBox', constrainedProperty)
def valueHolder = scaffoldingContext.validateable."${propertyName}Property"()

Map modelAttributes = [:]
if (valueHolder.value != null) modelAttributes.value = valueHolder.value
List values = constrainedProperty.nullable ? [null] : []
values.addAll(EnumSet.allOf(valueHolder.enumType))
widgetAttributes.model = new DefaultComboBoxModel(values as Object[])

errorDecorator {
    comboBox(widgetAttributes)
    scaffoldingContext.bind(getVariable(propertyName), 'selectedItem',
        valueHolder, constrainedProperty)
}