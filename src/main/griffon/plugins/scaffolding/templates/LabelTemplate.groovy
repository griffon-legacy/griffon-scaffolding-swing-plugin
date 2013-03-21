package griffon.plugins.scaffolding.templates

Map widgetAttributes = scaffoldingContext.widgetAttributes('label', constrainedProperty)
if (!widgetAttributes.containsKey('constraints')) widgetAttributes.constraints = 'top, grow'

errorDecorator {
    label(widgetAttributes)
    scaffoldingContext.bind(getVariable(propertyName), 'text',
        scaffoldingContext.validateable."${propertyName}Property"(), constrainedProperty)
}