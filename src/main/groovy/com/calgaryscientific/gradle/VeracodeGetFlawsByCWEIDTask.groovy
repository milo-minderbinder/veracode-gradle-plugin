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
class VeracodeGetFlawsByCWEIDTask extends VeracodeTask {
    static final String NAME = 'veracodeGetFlawsByCWEID'
    private String build_id
    private String cweid
    private File detailedReportFile

    VeracodeGetFlawsByCWEIDTask() {
        description = "List the flaws by CWE ID of the given 'build_id' report"
        requiredArguments << 'build_id'
        build_id = project.findProperty("build_id")
        cweid = project.findProperty("cweid")
        detailedReportFile = new File("${project.buildDir}/veracode", "detailed-report-${build_id}.xml")
    }

    void run() {
        if (cweid) {
            VeracodeDetailedReport.printFlawListByCWEID(XMLIO.readXml(detailedReportFile), cweid)
        } else {
            VeracodeDetailedReport.printFlawInformationByCWEID(XMLIO.readXml(detailedReportFile))
        }
    }
}
