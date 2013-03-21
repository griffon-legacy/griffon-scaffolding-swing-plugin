package griffon.plugins.scaffolding.templates

String widgetNode = constrainedProperty.password ? 'passwordField' : 'textField'
Map widgetAttributes = scaffoldingContext.widgetAttributes(widgetNode, constrainedProperty)
widgetAttributes.editable = constrainedProperty.editable
if (!widgetAttributes.containsKey('constraints')) widgetAttributes.constraints = 'top, grow'

errorDecorator {
    "${widgetNode}"(widgetAttributes)
    scaffoldingContext.bind(getVariable(propertyName), 'text',
        scaffoldingContext.validateable."${propertyName}Property"(), constrainedProperty)
}