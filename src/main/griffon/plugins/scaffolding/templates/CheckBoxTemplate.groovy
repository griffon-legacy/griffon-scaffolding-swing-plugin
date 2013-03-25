package griffon.plugins.scaffolding.templates

def valueHolder = scaffoldingContext.validateable."${propertyName}Property"()

Map widgetAttributes = scaffoldingContext.widgetAttributes('checkBox', constrainedProperty)
if (valueHolder.value != null) widgetAttributes.selected = valueHolder.value

errorDecorator {
    checkBox(widgetAttributes)
    scaffoldingContext.bind(getVariable(propertyName), 'selected',
        valueHolder, constrainedProperty)
}
