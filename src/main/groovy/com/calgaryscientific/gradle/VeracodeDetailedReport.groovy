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

@CompileStatic
class VeracodeDetailedReport {

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
        for (Node severity : XMLIO.getNodeList(xml, 'severity')) {
            for (Node category : XMLIO.getNodeList(severity, 'category')) {
                for (Node cwe : XMLIO.getNodeList(category, 'cwe')) {
                    for (Node flaw : XMLIO.getNodeList(cwe, 'staticflaws', 'flaw')) {
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
        List<Node> openFlaws = []
        for (Node flaw : flaws) {
            String status = flaw.attribute('remediation_status')
            if (status == "Open" || status == "New" || status != "Fixed") {
                openFlaws.add(flaw)
            }
        }
        return openFlaws
    }

}
