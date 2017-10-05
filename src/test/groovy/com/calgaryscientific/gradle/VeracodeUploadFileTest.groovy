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

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class VeracodeUploadFileTest extends TestCommonSetup {

    File filelistFile = getResource('filelist-1.1.xml')

    String errorXMLResponse = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<error>Could not upload file</error>
'''

    VeracodeUploadFileTask UploadFileTaskSetup() {
        // Setup project with plugin
        Project project = new ProjectBuilder().build()
        project.plugins.apply('com.calgaryscientific.gradle.veracode')
        // Setup dummy files to upload
        VeracodeSetup vs = new VeracodeSetup()
        vs.filesToUpload = project.fileTree(dir: testProjectDir.root, include: '**/*').getFiles()
        project.veracodeSetup.filesToUpload = vs.filesToUpload
        // Get task from project
        VeracodeUploadFileTask task = project.tasks.getByName("veracodeUploadFile") as VeracodeUploadFileTask
        // Don't delay unit tests
        task.waitTimeBetweenAttempts = "0"
        // Mock VeracodeAPI calls
        VeracodeAPI veracodeAPIMock = Mock(VeracodeAPI, constructorArgs: ["", "", null, null])
        task.veracodeAPI = veracodeAPIMock
        return task
    }

    def 'Test VeracodeUploadFile Task'() {
        given:
        VeracodeUploadFileTask task = UploadFileTaskSetup()

        when:
        task.run()

        then:
        1 * task.veracodeAPI.uploadFile(_, _) >> {
            return new String(filelistFile.readBytes())
        }
    }

    def 'Test VeracodeUploadFile Task failure'() {
        given:
        VeracodeUploadFileTask task = UploadFileTaskSetup()

        when:
        task.run()

        then:
        10 * task.veracodeAPI.uploadFile(_, _) >> {
            return errorXMLResponse
        }
        def e = thrown(GradleException)
        e.toString().contains("ERROR: Could not upload file")
    }

    def 'Test VeracodeUploadFile printFileUploadStatus'() {
        given:
        def os = mockSystemOut()
        Node xml = XMLIO.parse(filelistFile)

        when:
        VeracodeUploadFileTask.printFileUploadStatus(xml)
        def is = getSystemOut(os)
        restoreStdout()

        then:
        assert is.readLines() == ['file1=Uploaded', 'file2=Uploaded', 'file3=Uploaded']
    }
}
