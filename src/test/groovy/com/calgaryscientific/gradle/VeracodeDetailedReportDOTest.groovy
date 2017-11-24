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

class VeracodeDetailedReportDOTest extends TestCommonSetup {
    File detailedReportFile = getResource('detailedreport-1.5.xml')

    def 'Test getting softwareCompositionAnalysis CSV report'() {
        given:
        Node xml = XMLIO.parse(detailedReportFile)
        File softwareCompositionAnalysisFile = testProjectDir.newFile('software-composition-analysis.csv')

        when:
        List<List<String>> rows = VeracodeDetailedReport.softwareCompositionAnalysisRows(xml)
        VeracodeDetailedReport.writeCSV(softwareCompositionAnalysisFile, rows)
        List<String> output = softwareCompositionAnalysisFile.readLines()

        then:
        assert output[0] == 'library,file_name,vendor,description,cve_id,cwe_id,cvss_score,severity,cve_summary'
        assert output[1] == 'lib1,filename1,vendor1,description1,CVE-2017-123,CWE-1,5,3,cve_summary_1'
        assert output[2] == 'lib1,filename1,vendor1,description1,CVE-2017-456,CWE-2,4,3,cve_summary_2'
    }

    def 'Test getting CSV report for all flaws'() {
        given:
        Node xml = XMLIO.parse(detailedReportFile)
        File flawsFile = testProjectDir.newFile('buildinfo-flaws.csv')

        when:
        List<List<String>> flawRows = VeracodeDetailedReport.getFlawRowsFromDetailedReport(xml)
        VeracodeDetailedReport.writeCSV(flawsFile, flawRows)
        List<String> flawsOutput = flawsFile.readLines()

        then:
        assert flawsOutput[0] == 'issueid,severity,exploitLevel,categoryid,cweid,categoryname,date_first_occurrence,remediation_status,remediationeffort,mitigation_status,mitigation_status_desc,description,module,sourcefilepath,sourcefile,line,functionprototype,functionrelativelocation,type,mitigations,annotations,mitigations_xml,annotations_xml'
        assert flawsOutput[1] == "123,5,0,3,121,Stack-based Buffer Overflow,2017-06-18 16:22:39 UTC,Fixed,2,proposed,Mitigation Proposed,This call to vsprintf() contains a buffer overflow...,lib1.dll,path1,chunk.c,305,void fun(...),57,vsprintf,,,</>,</>"
        assert flawsOutput[2] == "123,5,0,3,121,Stack-based Buffer Overflow,2017-06-18 16:22:39 UTC,New,2,proposed,Mitigation Proposed,This call to vsprintf() contains a buffer overflow...,lib1.dll,path1,chunk.c,305,void fun(...),57,vsprintf,,,</>,</>"
        // TODO: Gradle isn't properly asserting flawsOutput[3]. Seems related to the \n char
        assert flawsOutput[3] == "181,5,0,18,78,Improper Neutralization of Special Elements used in an OS Command ('OS Command Injection'),2017-04-15 20:08:51 UTC,Open,3,proposed,Mitigation Proposed,This call to java.lang.ProcessBuilder.start() contains a command injection flaw...,moudle\$something,path,module.java,115,void run(),49,java.lang...,\"action: Mitigate by Design, date: 2017-11-10 18:23:22 UTC, user: User Name"
    }

    def 'Test getting CSV report for open flaws'() {
        given:
        Node xml = XMLIO.parse(detailedReportFile)
        File openFlawsFile = testProjectDir.newFile('buildinfo-open-flaws.csv')

        when:
        List<List<String>> openFlawRows = VeracodeDetailedReport.getOpenFlawRowsFromDetailedReport(xml)
        VeracodeDetailedReport.writeCSV(openFlawsFile, openFlawRows)
        List<String> openFlawsOutput = openFlawsFile.readLines()

        then:
        assert openFlawsOutput[0] == 'issueid,severity,exploitLevel,categoryid,cweid,categoryname,date_first_occurrence,remediation_status,remediationeffort,mitigation_status,mitigation_status_desc,description,module,sourcefilepath,sourcefile,line,functionprototype,functionrelativelocation,type,mitigations,annotations,mitigations_xml,annotations_xml'
        assert openFlawsOutput[1] == "123,5,0,3,121,Stack-based Buffer Overflow,2017-06-18 16:22:39 UTC,New,2,proposed,Mitigation Proposed,This call to vsprintf() contains a buffer overflow...,lib1.dll,path1,chunk.c,305,void fun(...),57,vsprintf,,,</>,</>"
        // TODO: Gradle isn't properly asserting openFlawsOutput[2]. Seems related to the \n char
        assert openFlawsOutput[2] == "181,5,0,18,78,Improper Neutralization of Special Elements used in an OS Command ('OS Command Injection'),2017-04-15 20:08:51 UTC,Open,3,proposed,Mitigation Proposed,This call to java.lang.ProcessBuilder.start() contains a command injection flaw...,moudle\$something,path,module.java,115,void run(),49,java.lang...,\"action: Mitigate by Design, date: 2017-11-10 18:23:22 UTC, user: User Name"
    }
}
