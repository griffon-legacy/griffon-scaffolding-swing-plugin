package griffon.plugins.scaffolding.templates

panel(id: 'content') {
    migLayout(layoutConstraints: 'wrap 2', columnConstraints: '[left][left, grow]')
    scaffoldingContext.validateable.constrainedProperties().each { propertyName, constrainedProperty ->
        if (!constrainedProperty.display) return
        setVariable('propertyName', propertyName)
        setVariable('constrainedProperty', constrainedProperty)
        Class labelerTemplate = scaffoldingContext.resolveLabeler(propertyName)
        build(labelerTemplate)
        Class widgetTemplate = scaffoldingContext.resolveWidget(propertyName)
        build(widgetTemplate)
    }
    button(cancelAction, constraints: 'skip, split 2, tag cancel')
    button(okAction, constraints: 'tag ok')

    keyStrokeAction(component: current,
        keyStroke: 'ESCAPE',
        condition: 'in focused window',
        action: cancelAction)
}