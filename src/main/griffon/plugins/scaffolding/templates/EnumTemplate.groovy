package griffon.plugins.scaffolding.templates

import javax.swing.DefaultComboBoxModel

Map widgetAttributes = scaffoldingContext.widgetAttributes('comboBox', constrainedProperty)
if (!widgetAttributes.containsKey('constraints')) widgetAttributes.constraints = 'top, grow'
def valueHolder = scaffoldingContext.validateable."${propertyName}Property"()

Map modelAttributes = [:]
if (valueHolder.value != null) modelAttributes.value = valueHolder.value
widgetAttributes.model = new DefaultComboBoxModel(EnumSet.allOf(valueHolder.enumType) as Object[])

errorDecorator {
    comboBox(widgetAttributes)
    scaffoldingContext.bind(getVariable(propertyName), 'selectedItem',
        valueHolder, constrainedProperty)
}