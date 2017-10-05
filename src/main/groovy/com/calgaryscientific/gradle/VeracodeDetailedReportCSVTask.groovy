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
     * Extracts the software_composition_analysis information of the detailed XML report and return a list of rows with it
     * @param xml - detailed report.
     * @param file - file to write the report to.
     *
     * Component tree:
     *
     * /component @description @file_name @library @max_cvss_score @sha1 @vendor @version @vulnerabilities
     *  /file_paths
     *      /file_path @value
     *  /vulnerabilities
     *      /vulnerability @cve_id @cve_summary @cvss_score @cwe_id @severity
     *  /violated_policy_rules
     */
    static List<List<String>> softwareCompositionAnalysisRows(Node xml) {
        List<List<String>> rows = []
        List<String> componentFields = ['library', 'file_name', 'vendor', 'description']
        List<String> vulnerabilityFields = ['cve_id', 'cwe_id', 'cvss_score', 'severity', 'cve_summary']
        // header row
        rows.add(componentFields + vulnerabilityFields)
        for (Node component : XMLIO.getNodeList(xml, 'software_composition_analysis', 'vulnerable_components', 'component')) {
            if ((component.attribute('vulnerabilities') as Integer) > 0) {
                for (Node vulnerability : XMLIO.getNodeList(component, 'vulnerabilities', 'vulnerability')) {
                    rows.add(XMLIO.getNodeAttributes(component, componentFields) + XMLIO.getNodeAttributes(vulnerability, vulnerabilityFields))
                }
            }
        }
        return rows
    }

    /**
     * Extracts the flaws information of the detailed XML report and return a list of rows with it
     *
     * @param xml
     * @return flawRows
     *
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
    static List<List<String>> extractFlawsFromDetailedReport(Node xml) {
        List<List<String>> rows = []
        List<String> flawFields = [
                'issueid',
                'severity',
                'exploitLevel',
                'categoryid',
                'cweid',
                'categoryname',
                'date_first_occurrence',
                'remediation_status',
                'remediationeffort',
                'mitigation_status',
                'mitigation_status_desc',
                'description',
                'module',
                'sourcefilepath',
                'sourcefile',
                'line',
                'functionprototype',
                'functionrelativelocation',
                'type',
        ]
        List<String> extraFields = [
                'mitigations',
                'annotations',
                'mitigations_xml',
                'annotations_xml'
        ]
        // header row
        rows.add(flawFields + extraFields)
        for (Node severity : XMLIO.getNodeList(xml, 'severity')) {
            for (Node category : XMLIO.getNodeList(severity, 'category')) {
                for (Node cwe : XMLIO.getNodeList(category, 'cwe')) {
                    for (Node flaw : XMLIO.getNodeList(cwe, 'staticflaws', 'flaw')) {
                        List<String> flawAttributes = XMLIO.getNodeAttributes(flaw, flawFields)
                        List<String> extraEntries = [
                                getMitigationsAnnotationsAsString(XMLIO.getNode(flaw, 'mitigations'), 'mitigation'),
                                getMitigationsAnnotationsAsString(XMLIO.getNode(flaw, 'annotations'), 'annotation'),
                                XMLIO.getNodeAsXMLString(XMLIO.getNode(flaw, 'mitigations'), false),
                                XMLIO.getNodeAsXMLString(XMLIO.getNode(flaw, 'annotations'), false)
                        ]
                        rows.add(flawAttributes + extraEntries)
                    }
                }
            }
        }
        return rows
    }

    /**
     * Given a mitigations or annotations Node it will return its formatted content
     * @param node
     * @return formatted string
     */
    static String getMitigationsAnnotationsAsString(Node node, String type) {
        XMLIO.getNodeList(node, type).collect { n ->
            List<String> nodeAttributes = XMLIO.getNodeAttributes(n, 'action', 'date', 'user', 'description')
            return sprintf("action: %s, date: %s, user: %s\ndescription: %s\n", nodeAttributes)
        }.join("\n")
    }

    /**
     * Write a list of rows to a CSV file
     * @param csvFile
     * @param rows
     */
    static void writeCSV(File csvFile, List<List<String>> rows) {
        BufferedWriter csvFileWriter = new BufferedWriter(csvFile.newWriter())
        CSVPrinter csvPrinter = new CSVPrinter(csvFileWriter, CSVFormat.DEFAULT)
        for (List<String> row : rows) {
            csvPrinter.printRecord(row)
        }
        csvFileWriter.flush()
        csvFileWriter.close()
        csvPrinter.close()
    }

    /**
     * Given a list of flaw rows, it extracts the open flaws into another row list.
     * It assumes remediation status is column 13
     * @param flawRows
     * @return
     */
    static List<List<String>> extractOpenFlawsFromFlawRows(List<List<String>> flawRows) {
        List<List<String>> rows = []
        int remediationStatusColumn = 7
        if (flawRows.size() < 1) {
            return rows
        }
        if (flawRows[0].size() < remediationStatusColumn) {
            println "ERROR: Wrong column size"
            return rows
        }
        // header row
        rows.add(flawRows[0])
        for (int i = 1; i < flawRows.size(); i++) {
            List<String> row = flawRows[i]
            if (row[remediationStatusColumn] == "Open" ||
                    row[remediationStatusColumn] == "New" ||
                    row[remediationStatusColumn] != "Fixed") {
                rows.add(row)
            }
        }
        return rows
    }

    void run() {
        Node xml = XMLIO.readXml(inputFile)
        List<List<String>> flaws = extractFlawsFromDetailedReport(xml)
        writeCSV(flawsDetailedReportCSVFile, flaws)
        List<List<String>> openFlaws = extractOpenFlawsFromFlawRows(flaws)
        writeCSV(openFlawsDetailedReportCSVFile, openFlaws)
        List<List<String>> scaRows = softwareCompositionAnalysisRows(xml)
        writeCSV(softwareCompositionAnalysisFile, scaRows)
        printf "Flaws Detailed Report CSV File: %s\n", flawsDetailedReportCSVFile
        printf "Open Flaws Detailed Report CSV File: %s\n", openFlawsDetailedReportCSVFile
        printf "Software Composition Analysis CSV File: %s\n", softwareCompositionAnalysisFile
    }
}
