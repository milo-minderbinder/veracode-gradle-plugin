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
class VeracodeGetFlawsByCWEIDTask extends VeracodeTask {
    static final String NAME = 'veracodeGetFlawsByCWEID'

    VeracodeGetFlawsByCWEIDTask() {
        description = "List the flaws by CWE ID of the given 'build_id' report"
        requiredArguments.addAll(['build_id'])
    }

    void run() {
        failIfNull(veracodeSetup.build_id)
        File detailedReportFile = new File(veracodeSetup.outputDir, "detailed-report-${veracodeSetup.build_id}.xml")
        if (veracodeSetup.cweid) {
            VeracodeDetailedReport.printFlawListByCWEID(XMLIO.readXml(detailedReportFile), veracodeSetup.cweid)
        } else {
            VeracodeDetailedReport.printFlawInformationByCWEID(XMLIO.readXml(detailedReportFile))
        }
    }
}
