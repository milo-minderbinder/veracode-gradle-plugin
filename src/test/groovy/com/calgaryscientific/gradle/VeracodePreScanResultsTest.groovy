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

class VeracodePreScanResultsTest extends TestCommonSetup {
    File preScanResultsFile = getResource('prescanresults-1.4.xml')

    def 'Test printModuleStatus'() {
        given:
        Node xml = XMLIO.parse(preScanResultsFile)
        def os = mockSystemOut()

        when:
        VeracodePreScanResults.printModuleStatus(xml)
        def is = getSystemOut(os)
        restoreStdout()

        then:
        assert is.readLines() == [
                'id=4 name="goodLib.jar" status="Supporting Files Compiled without Debug Symbols - X Files, PDB Files Missing - X Files"',
                'id=5 name="class1.jar" status="OK"',
                'id=6 name="badLib.dll" status="(Fatal)PDB Files Missing - 1 File"',
                'id=7 name="class2.jar" status="OK"',
                'id=8 name="class8.jar" status="OK"',
        ]
    }

    def 'Test VeracodeBeginScan extractModuleIds'() {
        given:
        Node xml = XMLIO.parse(preScanResultsFile)
        Set<String> whitelist = ['class1.jar', 'class2.jar']

        when:
        Set<String> moduleIds = VeracodePreScanResults.extractWhitelistModuleIds(xml, whitelist)

        then:
        assert moduleIds == ['5', '7'] as Set<String>
    }

    def 'Test VeracodeBeginScan extractModuleIds with missing classes'() {
        given:
        Node xml = XMLIO.parse(preScanResultsFile)
        Set<String> whitelist = ['class1.jar', 'class2.jar', 'class3.jar']

        when:
        Set<String> moduleIds = VeracodePreScanResults.extractWhitelistModuleIds(xml, whitelist)

        then:
        assert moduleIds == ['5', '7'] as Set<String>
    }
}
