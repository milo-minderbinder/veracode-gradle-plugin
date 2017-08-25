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

        // App tasks
        project.task(VeracodeBeginPreScanTask.NAME, type: VeracodeBeginPreScanTask)
        project.task(VeracodeBeginScanTask.NAME, type: VeracodeBeginScanTask)
        project.task(VeracodeCreateBuildTask.NAME, type: VeracodeCreateBuildTask)
        project.task(VeracodeDeleteBuildTask.NAME, type: VeracodeDeleteBuildTask)
        project.task(VeracodeGetAppInfoTask.NAME, type: VeracodeGetAppInfoTask)
        project.task(VeracodeGetAppListTask.NAME, type: VeracodeGetAppListTask)
        project.task(VeracodeGetBuildInfoTask.NAME, type: VeracodeGetBuildInfoTask)
        project.task(VeracodeGetBuildListTask.NAME, type: VeracodeGetBuildListTask)
        project.task(VeracodeGetFileListTask.NAME, type: VeracodeGetFileListTask)
        project.task(VeracodeGetPreScanResultsTask.NAME, type: VeracodeGetPreScanResultsTask)
        project.task(VeracodeRemoveFileTask.NAME, type: VeracodeRemoveFileTask)
        project.task(VeracodeUploadFileTask.NAME, type: VeracodeUploadFileTask)

        // Common tasks
        project.task(VeracodeDetailedReportTask.NAME, type: VeracodeDetailedReportTask)
        project.task(VeracodeDetailedReportCSVTask.NAME, type: VeracodeDetailedReportCSVTask)

        // TODO: Review these tasks
        project.task(ReportFlawsByTeamTask.NAME, type: ReportFlawsByTeamTask)
        project.task(ReportFlawsDiffTask.NAME, type: ReportFlawsDiffTask)
    }
}
