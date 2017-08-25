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

import org.gradle.api.Plugin
import org.gradle.api.Project

class VeracodePlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create('veracodeCredentials', VeracodeCredentials)

        project.task(VeracodeAppListTask.NAME, type: VeracodeAppListTask)
        project.task(VeracodeAppInfoTask.NAME, type: VeracodeAppInfoTask)
        project.task(VeracodeBuildListTask.NAME, type: VeracodeBuildListTask)
        project.task(VeracodeBuildInfoTask.NAME, type: VeracodeBuildInfoTask)
        project.task(VeracodeFileListTask.NAME, type: VeracodeFileListTask)
        project.task(VeracodeCreateBuildTask.NAME, type: VeracodeCreateBuildTask)
        project.task(VeracodeDeleteBuildTask.NAME, type: VeracodeDeleteBuildTask)
        project.task(GenerateToUploadTask.NAME, type: GenerateToUploadTask)
        project.task(VeracodeUploadTask.NAME, type: VeracodeUploadTask)
        project.task(VeracodePreScanTask.NAME, type: VeracodePreScanTask)
        project.task(VeracodePreScanResultsTask.NAME, type: VeracodePreScanResultsTask)
        project.task(PreScanModuleVerifyTask.NAME, type: PreScanModuleVerifyTask)
        project.task(VeracodeScanTask.NAME, type: VeracodeScanTask)
        def veracodeScanResultsTask = project.task(VeracodeScanResultsTask.NAME, type: VeracodeScanResultsTask)
        project.task(VeracodeScanResultsInCsvTask.NAME, type: VeracodeScanResultsInCsvTask, dependsOn: veracodeScanResultsTask)
        project.task(VeracodeRemoveFileTask.NAME, type: VeracodeRemoveFileTask)
        project.task(ReportFlawsByTeamTask.NAME, type: ReportFlawsByTeamTask)
        project.task(ReportFlawsDiffTask.NAME, type: ReportFlawsDiffTask)
    }
}
