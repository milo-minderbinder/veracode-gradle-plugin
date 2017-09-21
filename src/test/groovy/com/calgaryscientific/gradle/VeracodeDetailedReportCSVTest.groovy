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

class VeracodeDetailedReportCSVTest extends VeracodeTaskTest {
    File detailedReportFile = getResource('detailedreport-1.5.xml')

    def 'Test softwareCompositionAnalysisCSV'() {
        given:
        Node xml = XMLIO.parse(detailedReportFile)
        File softwareCompositionAnalysisFile = testProjectDir.newFile('software-composition-analysis.csv')

        when:
        List<List<String>> rows = VeracodeDetailedReportCSVTask.softwareCompositionAnalysisRows(xml)
        VeracodeDetailedReportCSVTask.writeCSV(softwareCompositionAnalysisFile, rows)
        List<String> output = softwareCompositionAnalysisFile.readLines()

        then:
        assert output[0] == 'library,file_name,vendor,description,cve_id,cwe_id,cvss_score,severity,cve_summary'
        assert output[1] == 'lib1,filename1,vendor1,description1,CVE-2017-123,CWE-1,5,3,cve_summary_1'
        assert output[2] == 'lib1,filename1,vendor1,description1,CVE-2017-456,CWE-2,4,3,cve_summary_2'
    }
    def 'Test flawReportCSV'() {
        given:
        Node xml = XMLIO.parse(detailedReportFile)
        File flawsFile = testProjectDir.newFile('buildinfo-flaws.csv')
        File openFlawsFile = testProjectDir.newFile('buildinfo-open-flaws.csv')

        when:
        List<List<String>> flawRows = VeracodeDetailedReportCSVTask.extractFlawsFromDetailedReport(xml)
        VeracodeDetailedReportCSVTask.writeCSV(flawsFile, flawRows)
        List<List<String>> openFlawRows = VeracodeDetailedReportCSVTask.extractOpenFlawsFromFlawRows(flawRows)
        VeracodeDetailedReportCSVTask.writeCSV(openFlawsFile, openFlawRows)
        List<String> flawsOutput = flawsFile.readLines()
        List<String> openFlawsOutput = openFlawsFile.readLines()

        then:
        assert flawsOutput[0] == 'Category ID,Category Name,CWE ID,Date,Description,Exploit Level,Function Prototype,Function Relative Location,Issue ID,Line,Mitigation Status,Mitigation StatusDesc,Module,Remediation Status,Remediation Effort,Severity,Source File,Source File Path,Type,Mitigations,Annotations'
        assert flawsOutput[1] == "3,Stack-based Buffer Overflow,121,2017-06-18 16:22:39 UTC,This call to vsprintf() contains a buffer overflow...,0,void fun(...),57,123,305,proposed,Mitigation Proposed,lib1.dll,Fixed,2,5,chunk.c,path1,vsprintf,,"
        // TODO: Gradle isn't properly asserting flawsOutput[2]. Seems related to the \n char
        // assert flawsOutput[2] == "18,Improper Neutralization of Special Elements used in an OS Command ('OS Command Injection'),78,2017-04-15 20:08:51 UTC,This call to java.lang.ProcessBuilder.start() contains a command injection flaw...,0,void run(),49,181,115,proposed,Mitigation Proposed,moudle\$something,Open,3,5,module.java,path,java.lang...,\"action: Mitigate by Design, date: 2017-11-10 18:23:22 UTC, user: User Name\ndescription: ...\n\naction: Potential False Positive, date: 2017-11-10 20:59:28, user: User Name\ndescription: ...\n\""
        assert flawsOutput[2] == "18,Improper Neutralization of Special Elements used in an OS Command ('OS Command Injection'),78,2017-04-15 20:08:51 UTC,This call to java.lang.ProcessBuilder.start() contains a command injection flaw...,0,void run(),49,181,115,proposed,Mitigation Proposed,moudle\$something,Open,3,5,module.java,path,java.lang...,\"action: Mitigate by Design, date: 2017-11-10 18:23:22 UTC, user: User Name"
        assert openFlawsOutput[0] == 'Category ID,Category Name,CWE ID,Date,Description,Exploit Level,Function Prototype,Function Relative Location,Issue ID,Line,Mitigation Status,Mitigation StatusDesc,Module,Remediation Status,Remediation Effort,Severity,Source File,Source File Path,Type,Mitigations,Annotations'
        // TODO: Gradle isn't properly asserting openFlawsOutput[1]. Seems related to the \n char
        assert openFlawsOutput[1] == "18,Improper Neutralization of Special Elements used in an OS Command ('OS Command Injection'),78,2017-04-15 20:08:51 UTC,This call to java.lang.ProcessBuilder.start() contains a command injection flaw...,0,void run(),49,181,115,proposed,Mitigation Proposed,moudle\$something,Open,3,5,module.java,path,java.lang...,\"action: Mitigate by Design, date: 2017-11-10 18:23:22 UTC, user: User Name"
    }
}
