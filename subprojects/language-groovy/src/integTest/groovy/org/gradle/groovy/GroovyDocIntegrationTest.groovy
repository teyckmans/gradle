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

package org.gradle.groovy

import org.apache.commons.lang.StringEscapeUtils
import org.gradle.integtests.fixtures.MultiVersionIntegrationSpec
import org.gradle.integtests.fixtures.TargetCoverage
import org.gradle.testing.fixture.GroovydocCoverage
import spock.lang.Issue

@TargetCoverage({GroovydocCoverage.ALL_COVERAGE})
class GroovyDocIntegrationTest extends MultiVersionIntegrationSpec {

    @Issue("https://issues.gradle.org/browse/GRADLE-3116")
    def "can run groovydoc"() {
        when:
        buildFile << """
            apply plugin: "groovy"

            repositories {
                mavenCentral()
            }

            dependencies {
                compile "org.codehaus.groovy:${module}:${version}"
            }
        """

        file("src/main/groovy/pkg/Thing.groovy") << """
            package pkg

            class Thing {}
        """

        then:
        succeeds "groovydoc"

        and:
        def text = file('build/docs/groovydoc/pkg/Thing.html').text
        def generatedBy = (text =~ /Generated by groovydoc \((.+?)\)/)

        generatedBy // did match
        generatedBy[0][1] == version

        where:
        module << ['groovy']
    }

    @Issue("https://issues.gradle.org/browse/GRADLE-3349")
    def "changes to overview causes groovydoc to be out of date"() {
        File overviewFile = file("overview.html")
        String escapedOverviewPath = StringEscapeUtils.escapeJava(overviewFile.absolutePath)

        when:
        buildFile << """
            apply plugin: "groovy"

            repositories {
                mavenCentral()
            }

            dependencies {
                compile "org.codehaus.groovy:${module}:${version}"
            }

            groovydoc {
                overviewText = resources.text.fromFile("${escapedOverviewPath}")
            }
        """

        overviewFile.text = """
<b>Hello World</b>
"""
        file("src/main/groovy/pkg/Thing.groovy") << """
            package pkg

            class Thing {}
        """

        then:
        succeeds "groovydoc"

        and:
        def overviewSummary = file('build/docs/groovydoc/overview-summary.html')
        overviewSummary.exists()
        overviewSummary.text.contains("Hello World")

        when:
        overviewFile.text = """
<b>Goodbye World</b>
"""
        and:
        succeeds "groovydoc"
        then:
        result.assertTaskNotSkipped(":groovydoc")
        overviewSummary.text.contains("Goodbye World")

        where:
        module << ['groovy']
    }

    @Issue(["GRADLE-3174", "GRADLE-3463"])
    def "output from Groovydoc generation is logged"() {
        when:
        buildScript """
            apply plugin: "groovy"

            repositories {
                mavenCentral()
            }

            dependencies {
                compile "org.codehaus.groovy:groovy:${version}"
            }
        """

        file("src/main/groovy/pkg/Thing.java") << """
            package pkg;

            import java.util.ArrayList;
            import java.util.List;

            public class Thing {
                   private List<String> firstOrderDepsWithoutVersions = new ArrayList<>(); // this cannot be parsed by the current groovydoc parser
            }
        """

        then:
        succeeds 'groovydoc'
        outputContains '[ant:groovydoc]'
    }
}
