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

class VeracodeGetPreScanResultsSandboxTest extends TestCommonSetup {
    File preScanResultsFile = getResource('prescanresults-1.4.xml')

    def 'Test veracodeSandboxGetPreScanResults Task'() {
        given:
        def os = mockSystemOut()
        def task = taskSetup('veracodeSandboxGetPreScanResults')
        task.veracodeSetup.app_id = '123'
        task.veracodeSetup.sandbox_id = '456'
        task.veracodeSetup.build_id = '123'

        when:
        task.run()
        def is = getSystemOut(os)
        restoreStdout()

        then:
        1 * task.veracodeAPI.getPreScanResultsSandbox('123') >> {
            return new String(preScanResultsFile.readBytes())
        }
        assert is.readLine() == 'id=4 name="goodLib.jar" status="Supporting Files Compiled without Debug Symbols - X Files, PDB Files Missing - X Files"'
        assert is.readLine() == 'id=5 name="class1.jar" status="OK"'
        assert is.readLine() == 'id=6 name="badLib.dll" status="(Fatal)PDB Files Missing - 1 File"'
        assert is.readLine() == 'id=7 name="class2.jar" status="OK"'
    }
}

