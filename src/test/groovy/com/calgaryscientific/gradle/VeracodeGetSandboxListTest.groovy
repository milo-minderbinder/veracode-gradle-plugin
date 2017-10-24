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

class VeracodeGetSandboxListTest extends TestCommonSetup {
    File sandboxlistFile = getResource('sandboxlist-1.0.xml')

    def 'Test veracodeGetSandboxList Task'() {
        given:
        def os = mockSystemOut()
        def task = taskSetup('veracodeGetSandboxList')

        when:
        task.run()
        def is = getSystemOut(os)
        restoreStdout()

        then:
        1 * task.veracodeAPI.getSandboxList() >> {
            return new String(sandboxlistFile.readBytes())
        }
        assert is.readLine() == 'sandbox_id=123        last_modified=2017-03-21T15:28:17-04:00 owner=david.gamba@org.com name=sandbox1'
        assert is.readLine() == 'sandbox_id=124        last_modified=2017-05-08T00:58:11-04:00 owner=david.gamba@org.com name=sandbox2'
        assert is.readLine() == 'sandbox_id=125        last_modified=2017-05-18T14:42:59-04:00 owner=david.gamba@org.com name=sandbox3'
        assert is.readLine() == 'sandbox_id=126        last_modified=2017-07-15T00:59:29-04:00 owner=david.gamba@org.com name=sandbox4'
    }
}
