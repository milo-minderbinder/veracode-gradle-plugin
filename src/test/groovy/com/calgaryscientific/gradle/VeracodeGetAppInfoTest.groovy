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

class VeracodeGetAppInfoTest extends TestCommonSetup {
    File appinfoFile = getResource('appinfo-1.1.xml')

    def 'Test veracodeGetAppInfo Task'() {
        given:
        def os = mockSystemOut()
        def task = taskSetup('veracodeGetAppInfo')
        task.veracodeSetup.app_id = '123'

        when:
        task.run()
        def is = getSystemOut(os)
        restoreStdout()

        then:
        1 * task.veracodeAPI.getAppInfo() >> {
            return new String(appinfoFile.readBytes())
        }
        assert is.readLine() == 'app_id=1'
        assert is.readLine() == 'app_name=App name'
        assert is.readLine() == 'app_type=Information Access/Delivery/Mining/Portal'
    }
}

