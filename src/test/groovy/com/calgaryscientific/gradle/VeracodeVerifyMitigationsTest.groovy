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

class VeracodeVerifyMitigationsTest extends TestCommonSetup {
    def 'Test getting all files'() {
        given:
        def project = new ProjectBuilder().build()
        project.plugins.apply('com.calgaryscientific.gradle.veracode')
        VeracodeVerifyMitigationsTask task = project.tasks.getByName("veracodeVerifyMitigations") as VeracodeVerifyMitigationsTask
        List<File> repoFiles = task.getRecursiveFileList('./src/test/resources')

        expect:
        assert repoFiles.size() == 5
        assert task.findFileInRepo(repoFiles, "detailedreport-1.5.xml").size() == 1
        assert task.findFileInRepo(repoFiles, "detailedreport-1.5.xml")[0].name == "detailedreport-1.5.xml"
    }
    def 'Test getRepoFiles'() {
        given:
        def project = new ProjectBuilder().build()
        project.plugins.apply('com.calgaryscientific.gradle.veracode')
        VeracodeVerifyMitigationsTask task = project.tasks.getByName("veracodeVerifyMitigations") as VeracodeVerifyMitigationsTask
        HashMap<String,String> repoPathMap = new HashMap<>()
        repoPathMap.put('testClasses', './src/test/groovy/com/calgaryscientific/gradle')
        repoPathMap.put('resources', './src/test/resources')
        HashMap<String,List<File>> repoFilesMap = task.getRepoFiles(repoPathMap)

        expect:
        assert repoFilesMap.size() == 2
        assert repoFilesMap['resources'].size() == 5
    }

    def 'Test findFileInRepo'() {
        given:
        def project = new ProjectBuilder().build()
        project.plugins.apply('com.calgaryscientific.gradle.veracode')
        VeracodeVerifyMitigationsTask task = project.tasks.getByName("veracodeVerifyMitigations") as VeracodeVerifyMitigationsTask
        HashMap<String,String> repoPathMap = new HashMap<>()
        repoPathMap.put('testClasses', './src/test/groovy/com/calgaryscientific/gradle')
        repoPathMap.put('resources', './src/test/resources')
        HashMap<String,List<File>> repoFilesMap = task.getRepoFiles(repoPathMap)
        List<File> files = task.findFileInRepo(repoFilesMap, 'resources', 'detailedreport-1.5.xml')

        expect:
        assert files.size() == 1
        assert files[0].name == "detailedreport-1.5.xml"
    }

    def 'Test getFileLine'() {
        given:
        def project = new ProjectBuilder().build()
        project.plugins.apply('com.calgaryscientific.gradle.veracode')
        VeracodeVerifyMitigationsTask task = project.tasks.getByName("veracodeVerifyMitigations") as VeracodeVerifyMitigationsTask
        String line = task.getFileLine('./src/test/resources/detailedreport-1.5.xml', 5)
        assert line =~ /report_format_version="1.5"/
    }

    def 'LineMatch'() {

    }
}
