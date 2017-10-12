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

import groovy.transform.CompileStatic

@CompileStatic
class VeracodeDeleteBuildTask extends VeracodeTask {
    static final String NAME = 'veracodeDeleteBuild'
    private String app_id

    VeracodeDeleteBuildTask() {
        description = "Delete the most recent Veracode build for 'app_id', even if it has a completed scan"
        requiredArguments << 'app_id'
        app_id = project.findProperty("app_id")
        defaultOutputFile = new File("${project.buildDir}/veracode", "build-list-${app_id}.xml")
    }

    void run() {
        // TODO: Run clean UploadFileTask after deleting the build
        Node xml = XMLIO.writeXml(getOutputFile(), veracodeAPI.deleteBuild(app_id))
        VeracodeBuildList.printBuildList(xml)
        printf "report file: %s\n", getOutputFile()
    }
}
