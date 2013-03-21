package griffon.plugins.scaffolding.templates

int sizeThreshold = 250

if (constrainedProperty.minSize >= sizeThreshold || constrainedProperty.maxSize >= sizeThreshold ||
    constrainedProperty.size?.from >= sizeThreshold || constrainedProperty.size?.to >= sizeThreshold) {

    Map widgetAttributes = scaffoldingContext.widgetAttributes('textArea', constrainedProperty)
    widgetAttributes.editable = constrainedProperty.editable
    Map scrollPaneAttributes = widgetAttributes.remove('scrollPane') ?: [:]
    scrollPaneAttributes.constraints = widgetAttributes.remove('constraints')

    errorDecorator {
        scrollPane(scrollPaneAttributes) {
            textArea(textAreaAttributes)
            scaffoldingContext.bind(getVariable(propertyName), 'text',
                scaffoldingContext.validateable."${propertyName}Property"(), constrainedProperty)
        }
    }
} else {
    String widgetNode = constrainedProperty.password ? 'passwordField' : 'textField'
    Map widgetAttributes = scaffoldingContext.widgetAttributes(widgetNode, constrainedProperty)
    widgetAttributes.editable = constrainedProperty.editable

    errorDecorator {
        "${widgetNode}"(widgetAttributes)
        scaffoldingContext.bind(getVariable(propertyName), 'text',
            scaffoldingContext.validateable."${propertyName}Property"(), constrainedProperty)
    }
}