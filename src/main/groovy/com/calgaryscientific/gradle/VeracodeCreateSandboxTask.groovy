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
class VeracodeCreateSandboxTask extends VeracodeTask {
    static final String NAME = 'veracodeCreateSandbox'

    VeracodeCreateSandboxTask() {
        group = 'Veracode Sandbox'
        description = "Creates a new sandbox for the given 'app_id'"
        requiredArguments.addAll(['app_id', 'sandbox_name'])
    }

    File getOutputFile() {
        return new File(veracodeSetup.outputDir, "sandboxinfo-${app_id}-latest.xml")
    }

    void printSandboxInfo(Node xml) {
        XMLIO.getNodeList(xml, 'sandbox').each { sandbox ->
            printf "sandbox_id=%s sandbox_name=\"%s\" owner=%s date=%s\n",
                    XMLIO.getNodeAttributes(sandbox, 'sandbox_id', 'sandbox_name', 'owner', 'created_date')
        }
    }

    void run() {
        failIfNull(veracodeSetup.app_id, veracodeSetup.sandbox_name)
        Node xml = XMLIO.writeXmlWithErrorCheck(getOutputFile(), veracodeAPI.createSandbox(veracodeSetup.sandbox_name))
        printSandboxInfo(xml)
        printf "report file: %s\n", getOutputFile()
    }
}
