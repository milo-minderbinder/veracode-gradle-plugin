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

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.UnexpectedBuildFailure

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class VeracodePluginTest extends TestCommonSetup {
    def 'Test Task Existence'() {
        when:
        def project = new ProjectBuilder().build()
        project.plugins.apply('com.calgaryscientific.gradle.veracode')
        List<String> taskList = [
                'veracodeBeginPreScan',
                'veracodeBeginScan',
                'veracodeCreateBuild',
                'veracodeDeleteBuild',
                'veracodeDetailedReportCSV',
                'veracodeDetailedReport',
                'veracodeGetAppInfo',
                'veracodeGetAppList',
                'veracodeGetBuildInfo',
                'veracodeGetBuildList',
                'veracodeGetFileList',
                'veracodeGetPreScanResults',
                'veracodeRemoveFile',
                'veracodeUploadFile',
        ]

        then:
        for (String taskName : taskList) {
            assert project.tasks.getByName(taskName) != null
        }
    }

    def 'Test build failure when arguments are missing'() {
        given:
        buildFile << """
            plugins {
                id 'com.calgaryscientific.gradle.veracode'
            }
            task getBuildList (type: com.calgaryscientific.gradle.VeracodeGetBuildListTask) {
            }
        """

        when:
        def result = executeTask('getBuildList')

        then:
        def e = thrown(UnexpectedBuildFailure)
        e.toString().contains("Missing required arguments")
    }

    def 'Test error message on missing parameters'() {
        when:
        List<String> requiredArgs = ["app_id"]
        List<String> optionalArgs = ["build_id"]
        String correctUsage = VeracodeTask.correctUsage('VeracodeGetBuildList', requiredArgs, optionalArgs)

        then:
        correctUsage == "Missing required arguments for task VeracodeGetBuildList:\n" +
                "veracodeSetup {\n" +
                "  app_id=123\n" +
                " [build_id=123]\n" +
                "}\n"
    }

    def 'Test veracodeSetup usage'() {
        given:
        buildFile << """
            plugins {
                id 'com.calgaryscientific.gradle.veracode'
            }

            veracodeSetup {
                username = 'user'
                password = 'pass'
                id = 'id'
                key = 'key'
                filesToUpload = fileTree(dir: ".", include: "*").getFiles()
                moduleWhitelist = ['class1.jar', 'class2.jar']
            }
            task verify {
                doLast {
                    assert project.veracodeSetup.username == 'user'
                    assert project.veracodeSetup.password == 'pass'
                    assert project.veracodeSetup.id == 'id'
                    assert project.veracodeSetup.key == 'key'
                    def vc = project.findProperty('veracodeSetup')
                    assert vc.key == 'key'
                    assert vc.filesToUpload == [buildFile] as Set
                    vc.filesToUpload.add(buildFile)
                    assert vc.filesToUpload  == [buildFile, buildFile] as Set
                    vc.filesToUpload.addAll(fileTree(dir: ".", include: "*").getFiles())
                    assert vc.filesToUpload  == [buildFile, buildFile, buildFile] as Set
                    assert vc.moduleWhitelist  == ['class1.jar', 'class2.jar'] as Set
                }
            }
        """

        when:
        def result = executeTask('verify')

        then:
        result.task(":verify").outcome == SUCCESS
    }

    def 'Test veracodeSetup filesToUpload are properly set'() {
        given:
        def project = new ProjectBuilder().build()
        VeracodeSetup vs = new VeracodeSetup()
        vs.filesToUpload = project.fileTree(dir: testProjectDir.root, include: '**/*').getFiles()

        when:
        project.plugins.apply('com.calgaryscientific.gradle.veracode')
        project.veracodeSetup.filesToUpload = vs.filesToUpload

        then:
        VeracodeSetup vsRead = project.findProperty("veracodeSetup") as VeracodeSetup
        Set<File> expected = [buildFile] as Set
        assert vsRead.filesToUpload == expected
        VeracodeUploadFileTask task = project.tasks.getByName("veracodeUploadFile") as VeracodeUploadFileTask
        task.setupTask()
        assert task.getFileSet() == expected
        def _ = vsRead.filesToUpload.add(buildFile)
        assert vsRead.filesToUpload == [buildFile, buildFile] as Set
    }
}
