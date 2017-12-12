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
class VeracodeUpdateMitigationInfoTask extends VeracodeTask {
    static final String NAME = 'veracodeUpdateMitigationInfo'
    String build_id
    String flaw_id_list
    String action
    String comment

    VeracodeUpdateMitigationInfoTask() {
        description = "Updates flaw information for the given 'build_id' and 'flaw_id_list'"
        requiredArguments << 'build_id' << 'flaw_id_list' << 'action' << 'comment'
        build_id = project.findProperty("build_id")
        flaw_id_list = project.findProperty("flaw_id_list")
        action = project.findProperty("action")
        comment = project.findProperty("comment")
    }

    File getOutputFile() {
        VeracodeMitigationInfo.getFile("${project.buildDir}/veracode", build_id, flaw_id_list)
    }

    void run() {
        if (!VeracodeMitigationInfo.validAction(action)) {
            fail("action must be one of: 'comment', 'fp', 'appdesign', 'osenv', 'netenv', 'rejected', 'accepted'. Received: ${action}")
        }
        if (!VeracodeMitigationInfo.validComment(comment)) {
            fail("comment must not exceed 1024 chars")
        }
        log.info(String.format("build_id: %s, action: %s", build_id, action))
        log.info(String.format("flaw_id_list: %s", flaw_id_list))
        log.info(String.format("comment: %s\n\n", comment))
        Node mitigationInfo = XMLIO.writeXml(getOutputFile(),
                veracodeAPI.updateMitigationInfo(build_id, action, comment, flaw_id_list))
        VeracodeMitigationInfo.printMitigationInfo(mitigationInfo)
        // Mitigation Info Updates have errors that are not top level as all other tasks.
        // Custom error checking required.
        VeracodeMitigationInfo.failOnErrors(mitigationInfo, getOutputFile())
        printf "report file: %s\n", getOutputFile()
    }
}
