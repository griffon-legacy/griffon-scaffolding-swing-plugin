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
 */

/**
 * @author Andres Almiray
 */

eventPost_package_pluginEnd = {
    if (!compilingPlugin('scaffolding-swing')) return

    File templatesDir = new File("${projectTargetDir}/templates")
    ant.delete(dir: templatesDir)
    ant.mkdir(dir: templatesDir)

    ant.copy(todir: templatesDir) {
        fileset(dir: 'src/main/griffon/plugins/scaffolding/templates')
    }
    ant.replace(dir: templatesDir, includes: '*') {
        replacefilter(token: 'package griffon.plugins.scaffolding.templates', value: 'package templates.scaffolding')
    }

    File mvcDir = new File("${projectTargetDir}/mvc-templates")
    ant.delete(dir: mvcDir)
    ant.mkdir(dir: mvcDir)

    ant.copy(todir: mvcDir) {
        fileset(dir: 'griffon-app/models/griffon/plugins/scaffolding/templates')
        fileset(dir: 'griffon-app/views/griffon/plugins/scaffolding/templates')
        fileset(dir: 'griffon-app/controllers/griffon/plugins/scaffolding/templates')
    }
    ant.replace(dir: mvcDir, includes: '*') {
        replacefilter(token: 'package griffon.plugins.scaffolding.templates', value: '@artifact.package@')
        replacefilter(token: 'CommandObjectModel', value: '@artifact.name@')
        replacefilter(token: 'CommandObjectController', value: '@artifact.name@')
    }

    ant.zip(destfile: "${artifactPackageDirPath}/${artifactZipFileName}", update: true, filesonly: true) {
        zipfileset(dir: templatesDir, prefix: 'src/templates/scaffolding')
        zipfileset(dir: mvcDir, prefix: 'src/templates/artifacts')
    }
}