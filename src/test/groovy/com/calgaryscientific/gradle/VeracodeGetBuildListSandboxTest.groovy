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

class VeracodeGetBuildListSandboxTest extends TestCommonSetup {
    File buildlistSandboxFile = getResource('buildlist-sandbox-1.3.xml')

    def 'Test veracodeGetBuildList Task'() {
        given:
        def os = mockSystemOut()
        def task = taskSetup('veracodeSandboxGetBuildList')

        when:
        task.run()
        def is = getSystemOut(os)
        restoreStdout()

        then:
        1 * task.veracodeAPI.getBuildList(_,_) >> {
            return new String(buildlistSandboxFile.readBytes())
        }
        assert is.readLine() == 'app_id=1 sandbox_id=2 build_id=123        date=2017-09-11T12:21:17-04:00 version="app-scan-123"'
        assert is.readLine() == 'app_id=1 sandbox_id=2 build_id=124        date=null                      version="app-scan-456"'
        assert is.readLine() == 'app_id=1 sandbox_id=2 build_id=125        date=2017-10-05T11:33:36-04:00 version="app-scan-789"'
    }
}

