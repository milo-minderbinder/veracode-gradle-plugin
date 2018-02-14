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
import org.gradle.util.GFileUtils

@CompileStatic
class XMLIO {
    static Node parseText(String xml) {
        XmlParser xmlParser = new XmlParser()
        return xmlParser.parseText(xml)
    }

    static Node parse(File file) {
        XmlParser xmlParser = new XmlParser()
        return xmlParser.parse(file)
    }

    static Node writeXmlWithErrorCheck(File file, String content) {
        Node xml =  writeXml(file, content)
        if (xml.name() == 'error') {
            fail("ERROR: ${xml.text()}\nSee ${file} for details!")
        }
        xml
    }

    static Node writeXml(File file, String content) {
        GFileUtils.writeFile(content, file)
        new XmlParser().parseText(content)
    }

    static Node readXml(File file) {
        new XmlParser().parse(file)
    }

    static Node readXml(String filename) {
        readXml(new File(filename))
    }

    /**
     * getNode - Gets the first child node matching the given name.
     * When more than one name is provided, it will return the first child recursively.
     */
    static Node getNode(Node node, String... name) {
        return getNode(node, name.toList())
    }

    static Node getNode(Node node, List<String> name) {
        if (name.size() == 1) {
            List<Node> nodeList = node.get(name[0]) as List<Node>
            if (nodeList.size() >= 1) {
                return nodeList.get(0) as Node
            } else {
                return new Node(null, '')
            }
        } else if (name.size() > 1) {
            List<Node> nodeList = node.get(name[0]) as List<Node>
            if (nodeList.size() >= 1) {
                Node subNode = nodeList.get(0) as Node
                return getNode(subNode, name[1..-1])
            } else {
                return new Node(null, '')
            }
        }
        return new Node(null, '')
    }

    /**
     * getNodeList - For a list of nodes names it will iterate over the first node
     * matching the name until the last member in which case it will return the entire nodeList
     */
    static List<Node> getNodeList(Node node, String... name) {
        return getNodeList(node, name.toList())
    }

    static List<Node> getNodeList(Node node, List<String> name) {
        if (name.size() == 1) {
            return node.get(name[0]) as List<Node>
        } else if (name.size() > 1) {
            List<Node> nodeList = node.get(name[0]) as List<Node>
            if (nodeList.size() >= 1) {
                Node subNode = nodeList.get(0) as Node
                return getNodeList(subNode, name[1..-1])
            } else {
                return new ArrayList<Node>()
            }
        }
        return new ArrayList<Node>()
    }

    static List<String> getNodeAttributes(Node node, String... attributes){
        return getNodeAttributes(node, attributes.toList())
    }

    static List<String> getNodeAttributes(Node node, List<String> attributes){
        return attributes.collect { attr ->
            node.attribute(attr) as String
        }
    }

    static fail(String msg) {
        throw new GradleException(msg)
    }

    static String getNodeAsXMLString(Node node, boolean namespaceAware) {
        StringWriter str = new StringWriter();
        PrintWriter pw = new PrintWriter(str)
        XmlNodePrinter np = new groovy.util.XmlNodePrinter(pw)
        np.setNamespaceAware(namespaceAware)
        np.setPreserveWhitespace(false)
        np.print(node)
        return str.toString().trim()
    }
}
