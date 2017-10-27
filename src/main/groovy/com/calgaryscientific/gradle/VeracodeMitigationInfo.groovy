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
class VeracodeMitigationInfo {
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
}
