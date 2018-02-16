/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2017 Calgary Scientific Incorporated
 *
 * Copyright (c) 2013-2014 kctang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/

package com.calgaryscientific.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class TestCommonSetup extends Specification {
    TemporaryFolder testProjectDir
    File buildFile
    Boolean debug
    PrintStream stdout = System.out

    def setup() {
        testProjectDir = new TemporaryFolder()
        testProjectDir.create()
        buildFile = testProjectDir.newFile('build.gradle')
        debug = true
    }

    def cleanup() {
        testProjectDir.delete()
    }

    def executeTask(String... tasks) {
        GradleRunner.create()
                .withDebug(debug)
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments(tasks)
                .build()
    }

    def taskSetup(String name) {
        // Setup project with plugin
        Project project = new ProjectBuilder().build()
        project.plugins.apply('com.calgaryscientific.gradle.veracode')
        // Get task from project
        def task = project.tasks.getByName(name)
        // Mock VeracodeAPI calls
        VeracodeAPI veracodeAPIMock = Mock(VeracodeAPI, constructorArgs: [null, null, null])
        task.veracodeAPI = veracodeAPIMock
        task.veracodeSetup = new VeracodeSetup()
        return task
    }

    def mockSystemOut() {
        def os = new ByteArrayOutputStream()
        System.out = new PrintStream(os)
        return os
    }

    BufferedReader getSystemOut(def os) {
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray())
        InputStreamReader isr = new InputStreamReader(is)
        BufferedReader ir = new BufferedReader(isr)
        return ir
    }

    def restoreStdout() {
        System.out = stdout
    }

    File getResource(String filename) {
        new File(getClass().classLoader.getResource(filename).toURI())
    }

}
