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

class VeracodeUploadFileTest extends VeracodeTaskTest {
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
            // Return success response
            return '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<filelist xmlns="something" xmlns:xsi="something" filelist_version="1.1">
    <file file_id="1" file_md5="d98b6f5ccfce3799e9b60b5d78cc1" file_name="file1" file_status="Uploaded"/>
    <file file_id="2" file_md5="68a7d8468ca51bc46d5b72d485022" file_name="file2" file_status="Uploaded"/>
    <file file_id="3" file_md5="2459464ff4bf78dd6f09695069b52" file_name="file3" file_status="Uploaded"/>
</filelist>
'''
        }
    }

    def 'Test VeracodeUploadFile Task failure'() {
        given:
        VeracodeUploadFileTask task = UploadFileTaskSetup()

        when:
        task.run()

        then:
        10 * task.veracodeAPI.uploadFile(_, _) >> {
            // Return error response
            return '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<error>Could not upload file</error>
'''
        }
        def e = thrown(GradleException)
        e.toString().contains("ERROR: Could not upload file")
    }

    def 'Test VeracodeUploadFile printFileUploadStatus'() {
        given:
        def os = mockSystemOut()
        String xmlStr = '''
<filelist xmlns="something" xmlns:xsi="something" filelist_version="1.1">
    <file file_id="1" file_md5="d98b6f5ccfce3799e9b60b5d78cc1" file_name="file1" file_status="Uploaded"/>
    <file file_id="2" file_md5="68a7d8468ca51bc46d5b72d485022" file_name="file2" file_status="Uploaded"/>
    <file file_id="3" file_md5="2459464ff4bf78dd6f09695069b52" file_name="file3" file_status="Uploaded"/>
</filelist>
'''
        when:
        XmlParser xmlParser = new XmlParser()
        Node xml = xmlParser.parseText(xmlStr)
        VeracodeUploadFileTask.printFileUploadStatus(xml)
        def is = getSystemOut(os)
        restoreStdout()

        then:
        assert is.readLines() == ['file1=Uploaded', 'file2=Uploaded', 'file3=Uploaded']
    }
}
