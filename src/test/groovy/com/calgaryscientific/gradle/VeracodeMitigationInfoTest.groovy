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

class VeracodeMitigationInfoTest extends TestCommonSetup {
    File mitigationInfoFile = getResource('mitigationinfo-1.1.xml')

    def 'Test validAction'() {
        expect:
        assert VeracodeMitigationInfo.validAction("invalid") == false
        assert VeracodeMitigationInfo.validAction("comment") == true
        assert VeracodeMitigationInfo.validAction("fp") == true
        assert VeracodeMitigationInfo.validAction("appdesign") == true
        assert VeracodeMitigationInfo.validAction("osenv") == true
        assert VeracodeMitigationInfo.validAction("netenv") == true
        assert VeracodeMitigationInfo.validAction("rejected") == true
        assert VeracodeMitigationInfo.validAction("accepted") == true
    }

    def 'Test validComment'() {
        expect:
        assert VeracodeMitigationInfo.validComment('x'*1024) == true
        assert VeracodeMitigationInfo.validComment('x'*1025) == false
    }

    static boolean validComment(String comment) {
        if (comment.length() > 1024) {
            return false
        }
        return true
    }

    def 'Test printMitigationInfo'() {
        given:
        def os = mockSystemOut()
        Node xml = XMLIO.parse(mitigationInfoFile)

        when:
        VeracodeMitigationInfo.printMitigationInfo(xml)
        def is = getSystemOut(os)
        restoreStdout()

        then:
        assert is.readLine() == '='*80
        assert is.readLine() == "flaw_id: 123 | category: Improper Neutralization of Special Elements used in an OS Command ('OS Command Injection')"
        assert is.readLine() == ''
        assert is.readLine() == 'action: appdesign'
    }
}
