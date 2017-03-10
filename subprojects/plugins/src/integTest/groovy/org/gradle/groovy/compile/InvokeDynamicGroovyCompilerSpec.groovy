/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.groovy.compile

import org.gradle.integtests.fixtures.TargetVersions
import org.gradle.test.fixtures.file.LeaksFileHandles
import org.gradle.util.TestPrecondition
import spock.lang.Issue

@TargetVersions(['2.4.9:indy'])
@LeaksFileHandles
class InvokeDynamicGroovyCompilerSpec extends ApiGroovyCompilerIntegrationSpec {
    def canEnableAndDisableInvokeDynamicOptimization() {
        when:
        run("sanityCheck")

        then:
        noExceptionThrown()
    }

    String compilerConfiguration() {
        '''
tasks.withType(GroovyCompile) {
    groovyOptions.fork = false
}
        '''
    }

    @Override
    @Issue('gradle/core-issues/#125')
    protected boolean gradleLeaksIntoAnnotationProcessor() {
        return !TestPrecondition.FIX_TO_WORK_ON_JAVA9.fulfilled
    }
}
