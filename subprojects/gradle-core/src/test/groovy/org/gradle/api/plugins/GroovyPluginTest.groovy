/*
 * Copyright 2007 the original author or authors.
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
 
package org.gradle.api.plugins

import org.gradle.api.Project
import org.gradle.api.internal.artifacts.configurations.Configurations
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.util.HelperUtil
import org.junit.Test
import static org.gradle.util.WrapUtil.*
import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

/**
 * @author Hans Dockter
 */
// todo Make test stronger
class GroovyPluginTest {
    private final Project project = HelperUtil.createRootProject()
    private final GroovyPlugin groovyPlugin = new GroovyPlugin()

    @Test public void appliesTheJavaPluginToTheProject() {
        groovyPlugin.use(project, project.getPlugins())

        assertTrue(project.getPlugins().hasPlugin(JavaPlugin));
    }

    @Test public void addsGroovyConfigurationToTheProject() {
        groovyPlugin.use(project, project.getPlugins())

        def configuration = project.configurations.getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME)
        assertThat(Configurations.getNames(configuration.extendsFrom, false), equalTo(toSet(GroovyPlugin.GROOVY_CONFIGURATION_NAME)))
        assertFalse(configuration.visible)
        assertFalse(configuration.transitive)

        configuration = project.configurations.getByName(GroovyPlugin.GROOVY_CONFIGURATION_NAME)
        assertThat(Configurations.getNames(configuration.extendsFrom, false), equalTo(toSet()))
        assertFalse(configuration.visible)
        assertFalse(configuration.transitive)
    }

    @Test public void addsTasksToTheProject() {
        groovyPlugin.use(project, project.getPlugins())

        def task = project.tasks[JavaPlugin.COMPILE_TASK_NAME]
        assertThat(task, instanceOf(GroovyCompile.class))
        assertThat(task.srcDirs, equalTo(project.convention.plugins.java.source.main.java.srcDirs as List))
        assertThat(task.groovySourceDirs, equalTo(project.convention.plugins.groovy.groovySrcDirs))

        task = project.tasks[JavaPlugin.COMPILE_TEST_TASK_NAME]
        assertThat(task, instanceOf(GroovyCompile.class))
        assertThat(task.srcDirs, equalTo(project.convention.plugins.java.source.test.java.srcDirs as List))
        assertThat(task.groovySourceDirs, equalTo(project.convention.plugins.groovy.groovyTestSrcDirs))

        task = project.tasks[JavaPlugin.JAVADOC_TASK_NAME]
        assertThat(task, instanceOf(Javadoc.class))
        assertThat(task.srcDirs, hasItems(project.convention.plugins.java.source.main.java.srcDirs as Object[]))
        assertThat(task.srcDirs, hasItems(project.convention.plugins.groovy.groovySrcDirs as Object[]))
        assertThat(task.exclude, hasItem('**/*.groovy'))

        task = project.tasks[GroovyPlugin.GROOVYDOC_TASK_NAME]
        assertThat(task, instanceOf(Groovydoc.class))
        assertThat(task.destinationDir, equalTo(project.convention.plugins.groovy.groovydocDir))
        assertThat(task.srcDirs, not(hasItems(project.convention.plugins.java.source.main.java.srcDirs as Object[])))
        assertThat(task.srcDirs, hasItems(project.convention.plugins.groovy.groovySrcDirs as Object[]))
    }

    @Test public void configuresAdditionalTasksDefinedByTheBuildScript() {
        groovyPlugin.use(project, project.getPlugins())
        
        def task = project.createTask('otherCompile', type: GroovyCompile)
        assertThat(task.classpath, sameInstance(project.convention.plugins.java.source.main.compileClasspath))
        assertThat(task.groovySourceDirs, hasItems(project.convention.plugins.groovy.groovySrcDirs as Object[]))

        task = project.createTask('otherJavadoc', type: Javadoc)
        assertThat(task.srcDirs, hasItems(project.convention.plugins.java.source.main.java.srcDirs as Object[]))
        assertThat(task.srcDirs, hasItems(project.convention.plugins.groovy.groovySrcDirs as Object[]))
        assertThat(task.exclude, hasItem('**/*.groovy'))

        task = project.createTask('otherGroovydoc', type: Groovydoc)
        assertThat(task.destinationDir, equalTo(project.convention.plugins.groovy.groovydocDir))
        assertThat(task.srcDirs, not(hasItems(project.convention.plugins.java.source.main.java.srcDirs as Object[])))
        assertThat(task.srcDirs, hasItems(project.convention.plugins.groovy.groovySrcDirs as Object[]))

    }
}
