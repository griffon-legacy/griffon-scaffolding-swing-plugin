package griffon.plugins.scaffolding.templates

def valueHolder = scaffoldingContext.validateable."${propertyName}Property"()

Map widgetAttributes = scaffoldingContext.widgetAttributes('slider', constrainedProperty)

if (constrainedProperty.min != null && constrainedProperty.max != null) {
    widgetAttributes.minimum = constrainedProperty.min
    widgetAttributes.maximum = constrainedProperty.max
} else if (constrainedProperty.range != null) {
    widgetAttributes.minimum = constrainedProperty.range.from
    widgetAttributes.maximum = constrainedProperty.range.to
}

widgetAttributes.value = valueHolder.value != null ? valueHolder.value : (widgetAttributes.minimum ?: 0)

errorDecorator {
    slider(widgetAttributes)
    scaffoldingContext.bind(getVariable(propertyName), 'value',
        valueHolder, constrainedProperty)
}