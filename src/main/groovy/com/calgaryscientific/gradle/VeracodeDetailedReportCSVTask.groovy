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

import groovy.transform.CompileStatic
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFiles

@CompileStatic
class VeracodeDetailedReportCSVTask extends VeracodeTask {
    static final String NAME = 'veracodeDetailedReportCSV'
    private String build_id
    private File flawsDetailedReportCSVFile
    private File openFlawsDetailedReportCSVFile
    private File softwareCompositionAnalysisFile

    VeracodeDetailedReportCSVTask() {
        description = "Get the Veracode Scan Report in CSV format based on the given 'build_id'"
        requiredArguments << 'build_id'
        dependsOn 'veracodeDetailedReport'
        build_id = project.findProperty("build_id")
        flawsDetailedReportCSVFile = new File("${project.buildDir}/veracode", "detailed-report-flaws-${build_id}.csv")
        openFlawsDetailedReportCSVFile = new File("${project.buildDir}/veracode", "detailed-report-open-flaws-${build_id}.csv")
        softwareCompositionAnalysisFile = new File("${project.buildDir}/veracode", "detailed-report-software-composition-analysis-${build_id}.csv")
    }

    // Scan reports can be modified by mitigation workflows so results shouldn't be cached.
    File getInputFile() {
        VeracodeDetailedReport.getFile("${project.buildDir}/veracode", build_id)
    }

    // Scan reports can be modified by mitigation workflows so results shouldn't be cached.
    List<File> getOutputFiles() {
        return [flawsDetailedReportCSVFile, openFlawsDetailedReportCSVFile, softwareCompositionAnalysisFile]
    }

    void run() {
        Node xml = XMLIO.readXml(inputFile)
        List<List<String>> flaws = VeracodeDetailedReport.getFlawRowsFromDetailedReport(xml)
        VeracodeDetailedReport.writeCSV(flawsDetailedReportCSVFile, flaws)
        List<List<String>> openFlaws = VeracodeDetailedReport.getOpenFlawRowsFromDetailedReport(xml)
        VeracodeDetailedReport.writeCSV(openFlawsDetailedReportCSVFile, openFlaws)
        List<List<String>> scaRows = VeracodeDetailedReport.softwareCompositionAnalysisRows(xml)
        VeracodeDetailedReport.writeCSV(softwareCompositionAnalysisFile, scaRows)
        printf "Flaws Detailed Report CSV File: %s\n", flawsDetailedReportCSVFile
        printf "Open Flaws Detailed Report CSV File: %s\n", openFlawsDetailedReportCSVFile
        printf "Software Composition Analysis CSV File: %s\n", softwareCompositionAnalysisFile
    }
}
