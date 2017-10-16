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

class VeracodeCreateSandboxTest extends TestCommonSetup {
    File sandboxInfoFile = getResource('sandboxinfo-1.2.xml')

    VeracodeCreateSandboxTask taskSetup() {
        // Setup project with plugin
        Project project = new ProjectBuilder().build()
        project.plugins.apply('com.calgaryscientific.gradle.veracode')
        // Get task from project
        VeracodeCreateSandboxTask task = project.tasks.getByName("veracodeCreateSandbox")
        // Mock VeracodeAPI calls
        VeracodeAPI veracodeAPIMock = Mock(VeracodeAPI, constructorArgs: ["", "", null, null])
        task.veracodeAPI = veracodeAPIMock
        return task
    }

    def 'Test veracodeCreateSandbox Task'() {
        given:
        def os = mockSystemOut()
        VeracodeCreateSandboxTask task = taskSetup()

        when:
        task.run()
        def is = getSystemOut(os)
        restoreStdout()

        then:
        1 * task.veracodeAPI.createSandbox(_,_) >> {
            return new String(sandboxInfoFile.readBytes())
        }
        assert is.readLine() == 'sandbox_id=123 sandbox_name="test-integration" owner=david.gamba&#x40;org.com date=2017-10-13T19:00:14-04:00'
    }
}

