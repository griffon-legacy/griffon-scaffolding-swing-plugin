package griffon.plugins.scaffolding.templates

String widgetNode = constrainedProperty.password ? 'passwordField' : 'textField'
Map widgetAttributes = scaffoldingContext.widgetAttributes(widgetNode, constrainedProperty)
widgetAttributes.editable = constrainedProperty.editable

errorDecorator {
    "${widgetNode}"(widgetAttributes)
    scaffoldingContext.bind(getVariable(propertyName), 'text',
        scaffoldingContext.validateable."${propertyName}Property"(), constrainedProperty)
}