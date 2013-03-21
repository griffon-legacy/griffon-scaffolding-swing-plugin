package griffon.plugins.scaffolding.templates

Map widgetAttributes = scaffoldingContext.widgetAttributes('label', constrainedProperty)

errorDecorator {
    label(widgetAttributes)
    scaffoldingContext.bind(getVariable(propertyName), 'text',
        scaffoldingContext.validateable."${propertyName}Property"(), constrainedProperty)
}