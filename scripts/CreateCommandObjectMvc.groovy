/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Andres Almiray
 */

import griffon.util.GriffonUtil

includeTargets << griffonScript('CreateIntegrationTest')

target(name: 'createCommandObjectMVC', description: "Creates a new CommandObject MVC Group",
    prehook: null, posthook: null) {
    if (isPluginProject && !isAddonPlugin) {
        println """You must create an Addon descriptor first.
Type in griffon create-addon then execute this command again."""
        System.exit(1)
    }

    promptForName(type: "MVC Group")
    def (pkg, name) = extractArtifactName(argsMap['params'][0])

    mvcPackageName = pkg ? pkg : ''
    mvcClassName = GriffonUtil.getClassNameRepresentation(name)
    mvcFullQualifiedClassName = "${pkg ? pkg : ''}${pkg ? '.' : ''}$mvcClassName"

    // -- compatibility
    argsMap['skip-model']      = argsMap['skip-model']      ?: argsMap.skipModel
    argsMap['skip-view']       = argsMap['skip-view']       ?: argsMap.skipView
    argsMap['skip-controller'] = argsMap['skip-controller'] ?: argsMap.skipController
    // -- compatibility

    if (!argsMap['skip-model']) {
        createArtifact(
            name:     mvcFullQualifiedClassName,
            suffix:   'CommandObjectModel',
            type:     'CommandObjectModel',
            template: 'CommandObjectModel',
            path:     'griffon-app/models')
    }

    if (!argsMap['skip-view']) {
        createArtifact(
            name:     mvcFullQualifiedClassName,
            suffix:   'CommandObjectView',
            type:     'CommandObjectView',
            template: 'CommandObjectView',
            path:     'griffon-app/views')
    }

    if (!argsMap['skip-controller']) {
        createArtifact(
            name:     mvcFullQualifiedClassName,
            suffix:   'CommandObjectController',
            type:     'CommandObjectController',
            template: 'CommandObjectController',
            path:     'griffon-app/controllers')
    }
}

setDefaultTarget(createCommandObjectMVC)
