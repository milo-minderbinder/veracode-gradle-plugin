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

import spock.lang.Specification

class XMLIOTest extends TestCommonSetup {
    def 'Test getNode gets first node when there are multiple options'() {
        given:
        File fileList = getResource('filelist-1.1.xml')

        when:
        Node xml = XMLIO.parse(fileList)

        then:
        assert XMLIO.getNode(xml,'file').attribute('file_id') == "1"
    }

    def 'Test getNode returns a Node object when the requested node does not exists'() {
        given:
        File fileList = getResource('filelist-1.1.xml')

        when:
        Node xml = XMLIO.parse(fileList)

        then:
        assert XMLIO.getNode(xml,'dummy').attribute('file_id') == null
    }

    def 'Test getNode traverses the tree using the first node when multiple entries provided'() {
        given:
        File preScanResults = getResource('prescanresults-1.4.xml')

        when:
        Node xml = XMLIO.parse(preScanResults)

        then:
        assert XMLIO.getNode(xml, 'module', 'file_issue').attribute('filename') == 'noDebug.ext'
    }

    def 'Test getNodeList returns a NodeList instead of a single Node'() {
        given:
        File fileList = getResource('filelist-1.1.xml')

        when:
        Node xml = XMLIO.parse(fileList)

        then:
        assert XMLIO.getNodeList(xml,'file')[0].attribute('file_id') == "1"
        assert XMLIO.getNodeList(xml,'file')[1].attribute('file_id') == "2"
        assert XMLIO.getNodeList(xml,'file')[2].attribute('file_id') == "3"
    }

    def 'Test getNodeList returns a NodeList object when the requested node does not exists or is not provided'() {
        given:
        File fileList = getResource('filelist-1.1.xml')

        when:
        Node xml = XMLIO.parse(fileList)

        then:
        assert XMLIO.getNodeList(xml).size() == 0
        assert XMLIO.getNodeList(xml, 'dummy').size() == 0
    }

    def 'Test getNodeList traverses the tree using the first node when multiple entries provided'() {
        given:
        File preScanResults = getResource('prescanresults-1.4.xml')

        when:
        Node xml = XMLIO.parse(preScanResults)

        then:
        assert XMLIO.getNodeList(xml, 'module', 'file_issue').size() == 3
        assert XMLIO.getNodeList(xml, 'module', 'file_issue')[0].attribute('filename') == 'noDebug.ext'
        assert XMLIO.getNodeList(xml, 'module', 'file_issue')[1].attribute('filename') == 'good.pdb'
        assert XMLIO.getNodeList(xml, 'module', 'file_issue')[2].attribute('filename') == 'notFound.pdb'
    }

    def 'Test getNodeAsString'() {
        when:
        given:
        File fileList = getResource('filelist-1.1.xml')

        when:
        Node xml = XMLIO.parse(fileList)
        Node fileNode = XMLIO.getNode(xml,'file')

        then:
        assert XMLIO.getNodeAsString(fileNode, false) == '<file file_id="1" file_md5="d98b6f5ccfce3799e9b60b5d78cc1" file_name="file1" file_status="Uploaded"/>'
        assert XMLIO.getNodeAsString(fileNode, true) == '<file xmlns="https://analysiscenter.veracode.com/schema/2.0/filelist" file_id="1" file_md5="d98b6f5ccfce3799e9b60b5d78cc1" file_name="file1" file_status="Uploaded"/>'
    }
}
