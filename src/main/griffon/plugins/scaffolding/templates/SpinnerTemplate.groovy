package griffon.plugins.scaffolding.templates

import griffon.plugins.scaffolding.atoms.EnumValue

Map widgetAttributes = scaffoldingContext.widgetAttributes('spinner', constrainedProperty)
def valueHolder = scaffoldingContext.validateable."${propertyName}Property"()

Map modelAttributes = [:]
if (valueHolder.value != null) modelAttributes.value = valueHolder.value

if (constrainedProperty.min != null && constrainedProperty.max != null) {
    modelAttributes.minimum = constrainedProperty.min
    modelAttributes.maximum = constrainedProperty.max
} else if (constrainedProperty.range != null) {
    modelAttributes.minimum = constrainedProperty.range.from
    modelAttributes.maximum = constrainedProperty.range.to
} else if (constrainedProperty.inList) {
    modelAttributes.minimum = constrainedProperty.inList[0]
    modelAttributes.maximum = constrainedProperty.inList[-1]
}
modelAttributes.value = modelAttributes.value ?: modelAttributes.minimum
if (modelAttributes.value == null) modelAttributes.remove('value')

if (Number.class.isAssignableFrom(valueHolder.valueType)) {
    modelAttributes.stepSize = widgetAttributes.remove('stepSize') ?: 1
    widgetAttributes.model = spinnerNumberModel(modelAttributes)
} else if (Date.class.isAssignableFrom(valueHolder.valueType) || Calendar.class.isAssignableFrom(valueHolder.valueType)) {
    modelAttributes.start = modelAttributes.remove('minimum')
    modelAttributes.end = modelAttributes.remove('maximum')
    modelAttributes.calendarField = widgetAttributes.constainsKey('calendarField') ? widgetAttributes.remove('calendarField') : Calendar.DAY_OF_MONTH
    widgetAttributes.model = spinnerDateModel(modelAttributes)
} else {
    if (constrainedProperty.inList) {
        modelAttributes.list = constrainedProperty.inList
    } else if (valueHolder instanceof EnumValue) {
        modelAttributes.list = constrainedProperty.nullable ? [null] : []
        modelAttributes.list.addAll(EnumSet.allOf(valueHolder.enumType))
    }
    widgetAttributes.model = spinnerListModel(modelAttributes)
}

errorDecorator {
    spinner(widgetAttributes)
    scaffoldingContext.bind(getVariable(propertyName), 'value',
        valueHolder, constrainedProperty)
}