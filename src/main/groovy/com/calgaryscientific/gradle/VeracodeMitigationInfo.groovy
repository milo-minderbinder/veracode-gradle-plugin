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
import org.gradle.api.GradleException

@CompileStatic
class VeracodeMitigationInfo {
    static File getFile(String dir, String build_id, String flaw_id_list) {
        if (flaw_id_list.split(',').size() > 1) {
            return new File(dir, "mitigationinfo-${build_id}-multiple-flaws.xml")
        }
        return new File(dir, "mitigationinfo-${build_id}-flaw${flaw_id_list}.xml")
    }

    static boolean validAction(String action) {
        if (action != "comment" &&
                action != "fp" &&
                action != "appdesign" &&
                action != "osenv" &&
                action != "netenv" &&
                action != "rejected" &&
                action != "accepted") {
            return false
        }
        return true
    }

    static boolean validComment(String comment) {
        if (comment.length() > 1024) {
            return false
        }
        return true
    }

    static void printMitigationInfo(Node xml) {
        XMLIO.getNodeList(xml, 'issue').each { issue ->
            printf "="*80+"\n"
            printf "flaw_id: %s | category: %s\n\n", XMLIO.getNodeAttributes(issue, 'flaw_id', 'category')
            XMLIO.getNodeList(issue, 'mitigation_action').each { mitigation_action ->
                printf "action: %s\ncomment: %s\ndate: %s\ndesc: %s\nreviewer: %s\n",
                        XMLIO.getNodeAttributes(mitigation_action, 'action', 'comment', 'date', 'desc', 'reviewer')
                printf "\n\n"
            }
            printf "\n\n"
        }
    }

    // Mitigation Info Updates have errors that are not top level as all other tasks.
    // Custom error checking required.
    static void failOnErrors(Node xml, File file) {
        printMitigationInfoErrors(xml)
        if (XMLIO.getNodeList(xml, 'error').size() > 0) {
            throw new GradleException("ERROR: Failed to update Mitigation Information\nSee ${file} for details!")
        }
    }

    static void printMitigationInfoErrors(Node xml) {
        XMLIO.getNodeList(xml, 'error').each { error ->
            printf "ERROR: flaw_id_list: %s | type: %s\n\n", XMLIO.getNodeAttributes(error, 'flaw_id_list', 'type')
        }
    }
}
