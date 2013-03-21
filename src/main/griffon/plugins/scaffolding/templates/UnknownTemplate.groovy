package griffon.plugins.scaffolding.templates

Map widgetAttributes = scaffoldingContext.widgetAttributes('textField', constrainedProperty)
widgetAttributes.editable = constrainedProperty.editable
if (!widgetAttributes.containsKey('constraints')) widgetAttributes.constraints = 'top, grow'

errorDecorator {
    textField(widgetAttributes)
    scaffoldingContext.bind(getVariable(propertyName), 'text',
        scaffoldingContext.validateable."${propertyName}Property"(), constrainedProperty)
}