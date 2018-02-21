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

class VeracodeBuildInfoTest extends TestCommonSetup {
    File buildInfoFile = getResource('buildinfo-1.4.xml')
    File buildInfoFileIncomplete = getResource('buildinfo-1.4-incomplete.xml')
    File buildInfoFileComplete = getResource('buildinfo-1.4-complete.xml')

    def 'Test VeracodeBuildInfo getBuildStatus'() {
        given:
        Node xml = XMLIO.parse(buildInfoFile)

        when:
        String buildStatus = VeracodeBuildInfo.getBuildStatus(xml)

        then:
        assert buildStatus == "Submitted to Engine"
    }

    def 'Test VeracodeBuildInfo isBuildReady'() {
        given:
        Node xml = XMLIO.parse(buildInfoFile)
        Node xmlIncomplete = XMLIO.parse(buildInfoFileIncomplete)
        Node xmlComplete = XMLIO.parse(buildInfoFileComplete)

        when:
        boolean resultsReady = VeracodeBuildInfo.isBuildReady(xml)
        boolean resultsReadyIncomplete = VeracodeBuildInfo.isBuildReady(xmlIncomplete)
        boolean resultsReadyComplete = VeracodeBuildInfo.isBuildReady(xmlComplete)

        then:
        assert resultsReady == false
        assert resultsReadyIncomplete == false
        assert resultsReadyComplete == true
    }

    def 'Test VeracodeBuildInfo printBuildInfo'() {
        given:
        Node xml = XMLIO.parse(buildInfoFile)
        def os = mockSystemOut()

        when:
        VeracodeBuildInfo.printBuildInfo(xml)
        def is = getSystemOut(os)
        restoreStdout()
        List<String> lines = is.readLines()

        then:
        assert lines.size() == 11
        assert lines.join("\n") == '''[build]
build_id=2
grace_period_expired=false
legacy_scan_engine=false
scan_overdue=false
submitter=CalgaryScientific
version=CSI-001
	[analysis_unit]
	analysis_type=Static
	engine_version=115614
	status=Submitted to Engine'''
    }
}
