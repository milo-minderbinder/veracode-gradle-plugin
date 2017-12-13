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

@CompileStatic
class VeracodeDetailedReport {

    static File getFile(String dir, String build_id) {
        return new File(dir, "detailed-report-${build_id}.xml")
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
        XMLIO.getNodeList(xml, 'software_composition_analysis', 'vulnerable_components', 'component').each { component ->
            if ((component.attribute('vulnerabilities') as Integer) > 0) {
                XMLIO.getNodeList(component, 'vulnerabilities', 'vulnerability').each { vulnerability ->
                    rows.add(XMLIO.getNodeAttributes(component, componentFields) + XMLIO.getNodeAttributes(vulnerability, vulnerabilityFields))
                }
            }
        }
        return rows
    }

    /**
     * Extracts the flaw Nodes from the detailed XML report
     *
     * @param xml
     * @return flaws
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
     *             /annotations
     *                /annotation @action @date @description @user
     * */
    static List<Node> getAllFlawsFromDetailedReportXML(Node xml) {
        List<Node> flaws = []
        XMLIO.getNodeList(xml, 'severity').each { severity ->
            XMLIO.getNodeList(severity, 'category').each { category ->
                XMLIO.getNodeList(category, 'cwe').each { cwe ->
                    XMLIO.getNodeList(cwe, 'staticflaws', 'flaw').each { flaw ->
                        flaws.add(flaw)
                    }
                }
            }
        }
        return flaws
    }

    /**
     * Extracts the Open flaw Nodes from the detailed XML report
     *
     * @param xml
     * @return flaws
     */
    static List<Node> getOpenFlawsFromDetailedReportXML(Node xml) {
        return filterOpenFlaws(getAllFlawsFromDetailedReportXML(xml))
    }

    /**
     * Extract Open flaws from a list of flaws.
     * @param flaws
     * @return flaws
     */
    static List<Node> filterOpenFlaws(List<Node> flaws) {
        flaws.findAll { flaw ->
            String status = flaw.attribute('remediation_status')
            (status == "Open" || status == "New" || (status != "Fixed" && status != "Mitigated"))
        }
    }

    /**
     * Extracts the flaws information of the detailed XML report and return a list of rows with it
     *
     * @param xml
     * @return flawRows
     *
     * */
    static List<List<String>> getFlawRowsFromDetailedReport(Node xml) {
        return getFlawsAsRows(VeracodeDetailedReport.getAllFlawsFromDetailedReportXML(xml))
    }

    /**
     * Turn a list of flaws into Rows
     *
     * flaw tree:
     *   /flaw @affects_policy_compliance
     *        -@categoryid
     *        -@categoryname
     *        -@cia_impact
     *        -@count
     *        -@cweid
     *        -@date_first_occurrence
     *        -@description
     *        -@exploitLevel
     *        -@functionprototype
     *        -@functionrelativelocation
     *        -@grace_period_expires
     *        -@issueid
     *        -@line
     *        -@mitigation_status
     *        -@mitigation_status_desc
     *        -@module
     *        -@note
     *        -@pcirelated
     *        -@remediation_status
     *        -@remediationeffort
     *        -@scope
     *        -@severity
     *        -@sourcefile
     *        -@sourcefilepath
     *        -@type
     *        /mitigations
     *          /mitigation @action @date @description @user
     *        /annotations
     *          /annotation @action @date @description @user
     *
     * @param flaws
     * @return rows
     */
    static List<List<String>> getFlawsAsRows(List<Node> flaws) {
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
        for (Node flaw : flaws) {
            List<String> flawAttributes = XMLIO.getNodeAttributes(flaw, flawFields)
            List<String> extraEntries = [
                    getMitigationsAnnotationsAsString(XMLIO.getNode(flaw, 'mitigations'), 'mitigation'),
                    getMitigationsAnnotationsAsString(XMLIO.getNode(flaw, 'annotations'), 'annotation'),
                    XMLIO.getNodeAsXMLString(XMLIO.getNode(flaw, 'mitigations'), false),
                    XMLIO.getNodeAsXMLString(XMLIO.getNode(flaw, 'annotations'), false)
            ]
            rows.add(flawAttributes + extraEntries)
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
    static List<List<String>> getOpenFlawRowsFromDetailedReport(Node xml) {
        return getFlawsAsRows(VeracodeDetailedReport.getOpenFlawsFromDetailedReportXML(xml))
    }

    static void printFlawListByCWEID(Node xml, String cweid) {
        List<Node> flaws = VeracodeDetailedReport.getOpenFlawsFromDetailedReportXML(xml)
        println "issueid, remediation_status, mitigation_status, module, sourcefilepath, sourcefile, line, type"
        flaws.findAll { flaw ->
            ((flaw.attribute('cweid') as String) == cweid)
        }.each { flaw ->
            printf "%s, %s, %s, %s, %s, %s, %s, %s\n",
                    XMLIO.getNodeAttributes(flaw, 'issueid', 'remediation_status', 'mitigation_status', 'module', 'sourcefilepath', 'sourcefile', 'line', 'type')
        }
    }

    static void printFlawInformationByCWEID(Node xml) {
        List<Node> flaws = VeracodeDetailedReport.getOpenFlawsFromDetailedReportXML(xml)
        List<List<String>> cweidInfo = getCWEIDInfoFromFlaws(flaws)
        println "CWEID, Severity, Count, Name"
        cweidInfo.each { printf "%5s, %8s, %5s, %s\n", it }
    }

    private static List<List<String>> getCWEIDInfoFromFlaws(List<Node> flaws) {
        List<String> cweids = flaws.collect { flaw -> flaw.attribute("cweid") as String }.unique()
        List<List<String>> cweidInfo = cweids.collect { cweid ->
            Node flawByCWEID = flaws.find { flaw -> flaw.attribute("cweid") == cweid }
            List<String> attributes = XMLIO.getNodeAttributes(flawByCWEID, 'severity', 'categoryname')
            String count = flaws.count { flaw -> flaw.attribute("cweid") == cweid } as String
            [cweid, attributes[0], count, attributes[1]]
        }
        return cweidInfo
    }
}
