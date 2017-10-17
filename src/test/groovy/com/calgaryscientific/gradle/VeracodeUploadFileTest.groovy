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

    def 'Test VeracodeUploadFile Task'() {
        given:
        def os = mockSystemOut()
        def task = taskSetup('veracodeUploadFile')
        // Don't delay unit tests
        task.waitTimeBetweenAttempts = "0"
        task.project.veracodeSetup.filesToUpload = task.project.fileTree(dir: testProjectDir.root, include: '**/*').getFiles()

        when:
        task.run()
        def is = getSystemOut(os)
        restoreStdout()

        then:
        1 * task.veracodeAPI.uploadFile(_) >> {
            return new String(filelistFile.readBytes())
        }
        assert is.readLine() == 'Processing build.gradle'
        assert is.readLine() == 'file1=Uploaded'
        assert is.readLine() == 'file2=Uploaded'
        assert is.readLine() == 'file3=Uploaded'
    }

    def 'Test VeracodeUploadFile Task failure'() {
        given:
        def task = taskSetup('veracodeUploadFile')
        // Don't delay unit tests
        task.waitTimeBetweenAttempts = "0"
        task.project.veracodeSetup.filesToUpload = task.project.fileTree(dir: testProjectDir.root, include: '**/*').getFiles()

        when:
        task.run()

        then:
        10 * task.veracodeAPI.uploadFile(_) >> {
            return errorXMLResponse
        }
        def e = thrown(GradleException)
        e.toString().contains("ERROR: Could not upload file")
    }
}
