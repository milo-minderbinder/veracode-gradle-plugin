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

import org.gradle.api.GradleException

class VeracodeUpdateMitigationInfoTest extends TestCommonSetup {
    File mitigationInfoFile = getResource('mitigationinfo-1.1.xml')
    File mitigationInfoErrorFile = getResource('mitigationinfo-1.1-error.xml')

    def 'Test veracodeUpdateMitigationInfo Task'() {
        given:
        def os = mockSystemOut()
        def task = taskSetup('veracodeUpdateMitigationInfo')
        task.build_id = "1"
        task.flaw_id_list = "123,456"
        task.action = "appdesign"
        task.comment = "meant to be..."

        when:
        task.run()
        def is = getSystemOut(os)
        restoreStdout()

        then:
        1 * task.veracodeAPI.updateMitigationInfo('1', 'appdesign', 'meant to be...', '123,456') >> {
            return new String(mitigationInfoFile.readBytes())
        }
        assert is.readLine() == '='*80
        assert is.readLine() == "flaw_id: 123 | category: Improper Neutralization of Special Elements used in an OS Command ('OS Command Injection')"
        assert is.readLine() == ''
        assert is.readLine() == 'action: appdesign'
    }

    def 'Test veracodeUpdateMitigationInfo action failure'() {
        given:
        def os = mockSystemOut()
        def task = taskSetup('veracodeUpdateMitigationInfo')
        task.build_id = "1"
        task.flaw_id_list = "123,456"
        task.action = "badAction"
        task.comment = "meant to be..."

        when:
        task.run()
        def is = getSystemOut(os)
        restoreStdout()

        then:
        def e = thrown(GradleException)
        e.toString().contains("action must be one of: ")
    }

    def 'Test veracodeUpdateMitigationInfo comment failure'() {
        given:
        def os = mockSystemOut()
        def task = taskSetup('veracodeUpdateMitigationInfo')
        task.build_id = "1"
        task.flaw_id_list = "123,456"
        task.action = "appdesign"
        task.comment = "x"*1025

        when:
        task.run()
        def is = getSystemOut(os)
        restoreStdout()

        then:
        def e = thrown(GradleException)
        e.toString().contains("comment must not exceed 1024 chars")
    }

    def 'Test veracodeUpdateMitigationInfo response failure'() {
        given:
        def os = mockSystemOut()
        def task = taskSetup('veracodeUpdateMitigationInfo')
        task.build_id = "1"
        task.flaw_id_list = "123,456"
        task.action = "appdesign"
        task.comment = "meant to be..."

        when:
        task.run()
        def is = getSystemOut(os)
        restoreStdout()

        then:
        1 * task.veracodeAPI.updateMitigationInfo('1', 'appdesign', 'meant to be...', '123,456') >> {
            return new String(mitigationInfoErrorFile.readBytes())
        }
        def e = thrown(GradleException)
        e.toString().contains("ERROR: Failed to update Mitigation Information")
    }
}
