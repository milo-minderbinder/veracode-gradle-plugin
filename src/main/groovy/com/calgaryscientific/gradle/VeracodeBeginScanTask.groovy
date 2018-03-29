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
class VeracodeBeginScanTask extends VeracodeTask {
    static final String NAME = 'veracodeBeginScan'

    VeracodeBeginScanTask() {
        description = "Begin a Veracode Scan for the given 'app_id'"
        requiredArguments.addAll(['app_id'])
    }

    File getOutputFile() {
        VeracodeBuildInfo.getFile(veracodeSetup.outputDir, app_id, null)
    }

    Set<String> getModuleWhitelist() {
        return veracodeSetup.moduleWhitelist
    }

    void run() {
        failIfNull(veracodeSetup.app_id)
        Node preScanResultsXML = XMLIO.writeXmlWithErrorCheck(VeracodePreScanResults.getFile(veracodeSetup.outputDir, app_id, null), veracodeAPI.getPreScanResults(null))
        VeracodePreScanResults.printModuleStatus(preScanResultsXML)
        Set<String> moduleIds = VeracodePreScanResults.extractWhitelistModuleIds(preScanResultsXML, getModuleWhitelist())
        println "Module IDs: " + moduleIds.join(",")
        Node xml = XMLIO.writeXmlWithErrorCheck(getOutputFile(), veracodeAPI.beginScan(moduleIds))
        VeracodeBuildInfo.printBuildInfo(xml)
        printf "report file: %s\n", getOutputFile()
    }
}
