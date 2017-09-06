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
class VeracodeGetPreScanResultsTask extends VeracodeTask {
    static final String NAME = 'veracodeGetPreScanResults'
    private String app_id
    private String build_id

    VeracodeGetPreScanResultsTask() {
        description = 'Gets the pre-scan results for the given application ID'
        requiredArguments << 'app_id'
        optionalArguments << 'build_id'
        app_id = project.findProperty('app_id')
        if (project.hasProperty('build_id')) {
            build_id = project.findProperty('build_id')
        }
        defaultOutputFile = new File("${project.buildDir}/veracode", 'pre-scan-results-latest.xml')
    }

    static void printModuleStatus(Node xml) {
        String app_id = xml.attribute('app_id')
        String build_id = xml.attribute('build_id')
        NodeList moduleList = xml.getAt("module") as NodeList
        for (int i = 0; i < moduleList.size(); i++) {
            Node moduleEntry = moduleList.get(i) as Node
            String id = moduleEntry.attribute('id')
            String name = moduleEntry.attribute('name')
            String status = moduleEntry.attribute('status')
            printf "app_id=%s build_id=%s id=%s name=\"%s\" status=\"%s\"\n",
                    app_id, build_id, id, name, status
        }
    }

    void run() {
        String response
        String file
        if (project.hasProperty('build_id')) {
            response = veracodeAPI.getPreScanResults(app_id, build_id)
            file = "pre-scan-results-${build_id}.xml"
        } else {
            response = veracodeAPI.getPreScanResults(app_id)
            file = getOutputFile()
        }
        Node xml = writeXml(file, response)
        printModuleStatus(xml)
    }
}
