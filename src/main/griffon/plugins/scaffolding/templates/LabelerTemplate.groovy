package griffon.plugins.scaffolding.templates

import static griffon.util.GriffonNameUtils.getNaturalName

label(scaffoldingContext.resolveMessage(propertyName + '.label', getNaturalName(propertyName) + ':'),
    constraints: 'top, left', id: propertyName + '_labeler',
    cssClass: constrainedProperty.nullable || constrainedProperty.blank ? '' : 'required')