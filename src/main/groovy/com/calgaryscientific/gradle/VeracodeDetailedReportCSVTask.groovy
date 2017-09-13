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
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
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
        description = 'Gets the Veracode scan results based on the build id passed in and convert it to CSV format'
        requiredArguments << 'build_id'
        dependsOn 'veracodeDetailedReport'
        build_id = project.findProperty("build_id")
        flawsDetailedReportCSVFile = new File("${project.buildDir}/veracode", "detailed-report-flaws-${build_id}.csv")
        openFlawsDetailedReportCSVFile = new File("${project.buildDir}/veracode", "detailed-report-open-flaws-${build_id}.csv")
        softwareCompositionAnalysisFile = new File("${project.buildDir}/veracode", "detailed-report-software-composition-analysis-${build_id}.csv")
    }

    VeracodeDetailedReportTask veracodeDetailedReport = new VeracodeDetailedReportTask()
    private File inputFile = veracodeDetailedReport.getOutputFile()

    // Scan results are not available until the full scan is complete so there is no risk in caching the report.
    @InputFile
    File getInputFile() {
        return inputFile
    }

    // Scan results are not available until the full scan is complete so there is no risk in caching the report.
    @OutputFiles
    List<File> getOutputFiles() {
        return [flawsDetailedReportCSVFile, openFlawsDetailedReportCSVFile, softwareCompositionAnalysisFile]
    }

    /**
     * Extracts the software_composition_analysis section of the detailed XML report and prints it as a separate csv file.
     * @param xml - detailed report.
     * @param file - file to write the report to.
     */
    static void softwareCompositionAnalysisCSV(Node xml, File file) {
        BufferedWriter writer = new BufferedWriter(file.newWriter())
        CSVPrinter csv = new CSVPrinter(writer, CSVFormat.DEFAULT)
        List<String> headerRow = ['library', 'file_name', 'vendor', 'description', 'cve_id', 'cwe_id', 'cvss_score', 'severity', 'cve_summary']
        csv.printRecord(headerRow)
        NodeList componentList = XMLIO.getNodeList(xml, 'software_composition_analysis', 'vulnerable_components', 'component')
        /**
         * Component tree:
         *
         * /component @description @file_name @library @max_cvss_score @sha1 @vendor @version @vulnerabilities
         *  /file_paths
         *      /file_path @value
         *  /vulnerabilities
         *      /vulnerability @cve_id @cve_summary @cvss_score @cwe_id @severity
         *  /violated_policy_rules
         */
        for (int i = 0; i < componentList.size(); i++) {
            Node component = componentList.get(i) as Node
            Integer vulnerabilities = component.attribute('vulnerabilities') as Integer
            if (vulnerabilities > 0) {
                String description = component.attribute('description')
                String library = component.attribute('library')
                String file_name = component.attribute('file_name')
                String vendor = component.attribute('vendor')
                NodeList vulnerabilityList = XMLIO.getNodeList(component, 'vulnerabilities', 'vulnerability')
                for (int j = 0; j < vulnerabilityList.size(); j++) {
                    Node vulnerability = vulnerabilityList.get(j) as Node
                    String cve_id = vulnerability.attribute('cve_id')
                    String cve_summary = vulnerability.attribute('cve_summary')
                    String cvss_score = vulnerability.attribute('cvss_score')
                    String cwe_id = vulnerability.attribute('cwe_id')
                    String severity = vulnerability.attribute('severity')
                    List<String> row = [library, file_name, vendor, description, cve_id, cwe_id, cvss_score, severity, cve_summary]
                    csv.printRecord(row)
                }
            }
        }
        writer.flush()
        writer.close()
        csv.close()
    }

    static void flawReportCSV(Node xml, File flawsDetailedReportCSVFile, File openFlawsDetailedReportCSVFile) {
        BufferedWriter flawsDetailedReportCSVWriter = new BufferedWriter(flawsDetailedReportCSVFile.newWriter())
        BufferedWriter openFlawsDetailedReportCSVWriter = new BufferedWriter(openFlawsDetailedReportCSVFile.newWriter())
        CSVPrinter csvFlawsReport = new CSVPrinter(flawsDetailedReportCSVWriter, CSVFormat.DEFAULT)
        CSVPrinter csvOpenFlawsReport = new CSVPrinter(openFlawsDetailedReportCSVWriter, CSVFormat.DEFAULT)
        List<String> headerRow = [
                'Category ID',
                'Category Name',
                'CWE ID',
                'Date',
                'Description',
                'Exploit Level',
                'Function Prototype',
                'Function Relative Location',
                'Issue ID',
                'Line',
                'Mitigation Status',
                'Mitigation StatusDesc',
                'Module',
                'Remediation Status',
                'Remediation Effort',
                'Severity',
                'Source File',
                'Source File Path',
                'Type'
        ]
        csvFlawsReport.printRecord(headerRow)
        csvOpenFlawsReport.printRecord(headerRow)
        NodeList severityList = XMLIO.getNodeList(xml, 'severity')
        for (int i = 0; i < severityList.size(); i++) {
            Node severity = severityList.get(i) as Node
            NodeList categoryList = XMLIO.getNodeList(severity, 'category')
            for (int j = 0; j < categoryList.size(); j++) {
                Node category = categoryList.get(j) as Node
                /**
                 * cwe tree:
                 *
                 * /cwe @certc @certcpp @certjava @cweid @cwename @owasp @pcirelated @sans
                 *     /description
                 *         /text @text
                 *     /staticflaws
                 *         /flaw @affects_policy_compliance
                 *              -@categoryid
                 *              -@categoryname
                 *              -@cia_impact
                 *              -@count
                 *              -@cweid
                 *              -@date_first_occurrence
                 *              -@description
                 *              -@exploitLevel
                 *              -@functionprototype
                 *              -@functionrelativelocation
                 *              -@grace_period_expires
                 *              -@issueid
                 *              -@line
                 *              -@mitigation_status
                 *              -@mitigation_status_desc
                 *              -@module
                 *              -@note
                 *              -@pcirelated
                 *              -@remediation_status
                 *              -@remediationeffort
                 *              -@scope
                 *              -@severity
                 *              -@sourcefile
                 *              -@sourcefilepath
                 *              -@type
                 *             /mitigations
                 *                /mitigation @action @date @description @user
                 *            /annotations
                 *                /annotation @action @date @description @user
                 * */
                NodeList cweList = XMLIO.getNodeList(category, 'cwe')
                for (int k = 0; k < cweList.size(); k++) {
                    Node cwe = cweList.get(k) as Node
                    NodeList flawList = XMLIO.getNodeList(cwe, 'staticflaws', 'flaw')
                    for (int l = 0; l < flawList.size(); l++) {
                        Node flaw = flawList.get(l) as Node
                        String flawCategoryID = flaw.attribute('categoryid')
                        String flawCategoryName = flaw.attribute('categoryname')
                        String flawCWEID = flaw.attribute('cweid')
                        String flawDate = flaw.attribute('date_first_occurrence')
                        String flawDescription = flaw.attribute('description')
                        String flawExploitLevel = flaw.attribute('exploitLevel')
                        String flawFunctionPrototype = flaw.attribute('functionprototype')
                        String flawFunctionRelativeLocation = flaw.attribute('functionrelativelocation')
                        String flawIssueID = flaw.attribute('issueid')
                        String flawLine = flaw.attribute('line')
                        String flawMitigationStatus = flaw.attribute('mitigation_status')
                        String flawMitigationStatusDesc = flaw.attribute('mitigation_status_desc')
                        String flawModule = flaw.attribute('module')
                        String flawRemediationStatus = flaw.attribute('remediation_status')
                        String flawRemediationEffort = flaw.attribute('remediationeffort')
                        String flawSeverity = flaw.attribute('severity')
                        String flawSourceFile = flaw.attribute('sourcefile')
                        String flawSourceFilePath = flaw.attribute('sourcefilepath')
                        String flawType = flaw.attribute('type')
                        List<String> row = [
                                flawCategoryID,
                                flawCategoryName,
                                flawCWEID,
                                flawDate,
                                flawDescription,
                                flawExploitLevel,
                                flawFunctionPrototype,
                                flawFunctionRelativeLocation,
                                flawIssueID,
                                flawLine,
                                flawMitigationStatus,
                                flawMitigationStatusDesc,
                                flawModule,
                                flawRemediationStatus,
                                flawRemediationEffort,
                                flawSeverity,
                                flawSourceFile,
                                flawSourceFilePath,
                                flawType
                        ]
                        csvFlawsReport.printRecord(row)
                        if (flawRemediationStatus == "Open") {
                            csvOpenFlawsReport.printRecord(row)
                        }
                    }
                }
            }
        }
        flawsDetailedReportCSVWriter.flush()
        flawsDetailedReportCSVWriter.close()
        openFlawsDetailedReportCSVWriter.flush()
        openFlawsDetailedReportCSVWriter.close()
        csvFlawsReport.close()
        csvOpenFlawsReport.close()
    }

    void run() {
        Node xml = XMLIO.readXml(inputFile)
        flawReportCSV(xml, flawsDetailedReportCSVFile, openFlawsDetailedReportCSVFile)
        softwareCompositionAnalysisCSV(xml, softwareCompositionAnalysisFile)
        printf "Flaws Detailed Report CSV File: %s\n", flawsDetailedReportCSVFile
        printf "Open Flaws Detailed Report CSV File: %s\n", openFlawsDetailedReportCSVFile
        printf "Software Composition Analysis CSV File: %s\n", softwareCompositionAnalysisFile
    }
}
