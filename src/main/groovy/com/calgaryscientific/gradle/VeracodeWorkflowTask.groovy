/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2017-2018 Calgary Scientific Incorporated
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

import groovy.transform.CompileStatic

@CompileStatic
class VeracodeWorkflowTask extends VeracodeTask {
    static final String NAME = 'veracodeWorkflow'

    VeracodeWorkflowTask() {
        description = "Run through the Veracode Workflow for the given 'app_id' using 'build_version' as the build identifier"
        requiredArguments.addAll(['app_id', 'build_version'])
        optionalArguments.addAll(['maxUploadAttempts', 'waitTimeBetweenAttempts', 'deleteUploadedArtifacts', 'ignoreFailure'])
    }

    Set<File> getFileSet() {
        return veracodeSetup.filesToUpload
    }

    Set<String> getModuleWhitelist() {
        return veracodeSetup.moduleWhitelist
    }

    void run() {
        try {
            VeracodeWorkflow.appWorkflow(veracodeAPI,
                    veracodeSetup.outputDir,
                    app_id,
                    veracodeSetup.build_version,
                    getFileSet(),
                    getModuleWhitelist(),
                    veracodeSetup.maxUploadAttempts,
                    veracodeSetup.waitTimeBetweenAttempts,
                    veracodeSetup.deleteUploadedArtifacts,
                    veracodeSetup.failWorkflowTasksOnNewFlaws
            )
        } catch (Exception e) {
            if (veracodeSetup.ignoreFailure) {
                println e.getMessage()
            } else {
                throw e
            }
        }
    }
}
