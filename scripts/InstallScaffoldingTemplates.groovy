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

import groovy.io.FileType
import org.springframework.core.io.Resource
import org.springframework.util.FileCopyUtils

import java.util.regex.Matcher
import java.util.regex.Pattern

import static griffon.util.ConfigUtils.getFilenameExtension
import static griffon.util.ConfigUtils.stripFilenameExtension
import static griffon.util.GriffonNameUtils.capitalize

/**
 * @author Andres Almiray
 */

includeTargets << griffonScript('_GriffonCreateArtifacts')

target(name: 'installScaffoldingTemplates', prehook: null, posthook: null,
    description: 'Install scaffolding templates in the current project') {
    if (isPluginProject || isApplicationProject) {
        Map<String, Map<String, TemplateInfo>> templates = [:]
        for (Resource pluginDir : pluginSettings.projectPluginDirectories) {
            File templateDir = new File("${pluginDir.file.absolutePath}/src/templates/scaffolding")
            if (templateDir.exists()) {
                def (pluginName, pluginVersion) = pluginNameAndVersion(pluginDir.file.name)
                templates[pluginName] = doListTemplates(templateDir)
            }
        }

        if (argsMap.list) {
            println """
Available Scaffolding Templates are listed below:
${'-' * 80}
${'Plugin'.padRight(25, ' ')}${'Name'.padRight(30, ' ')}FileType
${'-' * 80}"""

            templates.sort().each { String plugin, Map templateInfo ->
                printTemplateInfo(plugin, templateInfo)
            }
        } else {
            String templateSourcePlugin = argsMap.plugin ?: 'scaffolding-swing'
            if (!templates[templateSourcePlugin]) templateSourcePlugin = 'scaffolding-swing'
            resolveFileType()

            if (argsMap.template) {
                String templateName = capitalize(argsMap.template) + 'Template'
                TemplateInfo templateInfo = templates[templateSourcePlugin][templateName]
                if (!templateInfo) {
                    event 'StatusError', ["Plugin ${templateSourcePlugin} does not provide a Scaffolding template named ${templateName}"]
                    exit 1
                } else {
                    copyTemplate(templateInfo, fileType)
                }
            } else {
                templates[templateSourcePlugin].each { String name, TemplateInfo templateInfo ->
                    copyTemplate(templateInfo, fileType)
                }
            }
        }
    } else {
        event 'StatusError', ['Scaffolding templates are not available for this type of project.']
        exit 1
    }
}

setDefaultTarget(installScaffoldingTemplates)

private List pluginNameAndVersion(String str) {
    Pattern ARTIFACT_NAME_VERSION_PATTERN = Pattern.compile('([a-zA-Z0-9\\-/\\._+=]+?)-([0-9][a-zA-Z0-9\\-/\\.,\\]\\[\\(\\)_+=]+)')
    Matcher matcher = ARTIFACT_NAME_VERSION_PATTERN.matcher(str)
    [matcher[0][1], matcher[0][2]]
}

private Map doListTemplates(File directory) {
    Map<String, TemplateInfo> templates = [:]
    directory.eachFile(FileType.FILES) { File file ->
        String templateName = stripFilenameExtension(file.name)
        TemplateInfo templateInfo = templates.get(templateName)
        if (!templateInfo) {
            templateInfo = new TemplateInfo(name: templateName, location: file.parentFile)
            templates[templateName] = templateInfo
        }
        templateInfo.fileTypes << getFilenameExtension(file.name)
    }
    templates
}

private void printTemplateInfo(String location, Map templates) {
    int i = 0
    templates.sort().each { String name, TemplateInfo templateInfo ->
        println "${(i++ > 0 ? '' : location).padRight(25, ' ')}${templateInfo.name.padRight(30, ' ')}${templateInfo.fileTypes.sort().join(', ')}"
    }
}

private void copyTemplate(TemplateInfo templateInfo, String fileType) {
    File templateFile = new File("${templateInfo.location}/${templateInfo.name}.${fileType}")
    if (!templateFile.exists()) {
        templateFile = new File("${templateInfo.location}/${templateInfo.name}.groovy")
        fileType = 'groovy'
    }
    if (!templateFile.exists()) {
        event 'StatusError', ["Scaffolding templates ${templateFile} does not exist"]
        exit 1
    }
    File templatesDir = new File("${basedir}/src/templates/scaffolding")
    File outputFile = new File("${templatesDir}/${templateInfo.name}.${fileType}")
    ant.mkdir(dir: templatesDir)
    FileCopyUtils.copy(templateFile, outputFile)
    event 'StatusUpdate', ["Created template file ${outputFile}"]
}

class TemplateInfo {
    String name
    File location
    List fileTypes = []
}