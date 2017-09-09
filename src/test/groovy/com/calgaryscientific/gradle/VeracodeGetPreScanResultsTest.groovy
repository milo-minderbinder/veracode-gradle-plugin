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

class VeracodeGetPreScanResultsTest extends VeracodeTaskTest {
    static String successXMLResponse = '''
<prescanresults xmlns="something" xmlns:xsi="somthing" app_id="1" build_id="2" prescanresults_version="1.4">
  <module app_file_id="3" has_fatal_errors="false" id="4" is_dependency="false" name="goodLib.jar"
   status="Supporting Files Compiled without Debug Symbols - X Files, PDB Files Missing - X Files">
      <file_issue details="Compiled without debug symbols (Optional)" filename="noDebug.ext"/>
      <file_issue details="Found (Optional)" filename="good.pdb"/>
      <file_issue details="Not Found (Optional)" filename="notFound.pdb"/>
   </module>
   <module has_fatal_errors="false" id="5" name="class1.jar" status="OK">
      <file_issue details="Found (Optional)" filename="path.pdb"/>
   </module>
   <module has_fatal_errors="true" id="6" name="badLib.dll" status="(Fatal)PDB Files Missing - 1 File">
      <file_issue details="Not Found (Required)" filename="notFound.pdb"/>
   </module>
   <module has_fatal_errors="false" id="7" name="class2.jar" status="OK">
      <file_issue details="Found (Optional)" filename="path.pdb"/>
   </module>
</prescanresults>
'''

    def 'Test VeracodeGetPreScanResults printModuleStatus'() {
        given:
        def os = mockSystemOut()
        Node xml = parseXMLString(successXMLResponse)

        when:
        VeracodeGetPreScanResultsTask.printModuleStatus(xml)
        def is = getSystemOut(os)
        restoreStdout()

        then:
        assert is.readLines() == [
                'app_id=1 build_id=2 id=4 name="goodLib.jar" status="Supporting Files Compiled without Debug Symbols - X Files, PDB Files Missing - X Files"',
                'app_id=1 build_id=2 id=5 name="class1.jar" status="OK"',
                'app_id=1 build_id=2 id=6 name="badLib.dll" status="(Fatal)PDB Files Missing - 1 File"',
                'app_id=1 build_id=2 id=7 name="class2.jar" status="OK"',
        ]
    }
}
