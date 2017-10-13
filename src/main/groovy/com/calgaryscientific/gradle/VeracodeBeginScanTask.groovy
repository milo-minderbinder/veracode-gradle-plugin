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
class VeracodeBeginScanTask extends VeracodeTask {
    static final String NAME = 'veracodeBeginScan'
    private String app_id
    private Set<String> moduleWhitelist

    VeracodeBeginScanTask() {
        description = "Begin a Veracode Scan for the given 'app_id'"
        requiredArguments << 'app_id'
        dependsOn "veracodeGetPreScanResults"
        app_id = project.findProperty("app_id")
        defaultOutputFile = new File("${project.buildDir}/veracode", "build-info-${app_id}-latest.xml")
    }

    VeracodeGetPreScanResultsTask preScan = new VeracodeGetPreScanResultsTask()
    File preScanResultsOutputFile = preScan.getOutputFile()

    /**
     * Given Veracode's GetPreScanResults xml and a whitelist list of modules,
     * it will return the module IDs for the non fatal whitelisted modules.
     *
     * @param xml
     * @param whitelist
     * @return
     */
    static Set<String> extractWhitelistModuleIds(Node xml, Set<String> whitelist) {
        List<Node> nonFatalModules = filterOutFatalModules(XMLIO.getNodeList(xml, 'module'))
        List<Node> whitelistModules = getWhitelistModules(nonFatalModules, whitelist)
        printMissingWhitelistModules(whitelist, whitelistModules)
        return whitelistModules.collect { module ->
            module.attribute('id') as String
        }.toSet()
    }

    private static void printMissingWhitelistModules(Set<String> whitelist, List<Node> whitelistModules) {
        Set<String> missingWhitelistModules = getMissingWhitelistModules(whitelist, whitelistModules)
        if (missingWhitelistModules.size() > 0) {
            // TODO: Look into logging levels. The whole plugin is using print statements.
            printf "WARNING: Missing whitelist modules: ${missingWhitelistModules}\n"
        }
    }

    /**
     * Given a Veracode moduleList List<Node> (xml), it will filter out modules that are not fatal.
     *
     * @param moduleList
     * @return Map{ name: [id, status] }
     */
    private static List<Node> filterOutFatalModules(List<Node> moduleList) {
        moduleList.findAll { module ->
            !(module.attribute('status') as String).startsWith('(Fatal)')
        }
    }

    /**
     * Given a list of modules, it will return the subset of modules that are found in the whitelist.
     * @param modules
     * @param whitelist
     * @return Map{ name: [id, status] }
     */
    private
    static List<Node> getWhitelistModules(List<Node> modules, Set<String> whitelist) {
        modules.findAll { module ->
            if (whitelist.contains(module.attribute('name') as String)) {
                printf "Selecting module: %s: %s - %s\n", XMLIO.getNodeAttributes(module, 'id', 'name', 'status')
                return true
            }
        }
    }

    /**
     * Given a whitelist and a list of whitelistModules,
     * it will return the whitelist entries that aren't part of the whitelist modules.
     * @param whitelist
     * @param whitelistModules
     * @return
     */
    private
    static Set<String> getMissingWhitelistModules(Set<String> whitelist, List<Node> whitelistModules) {
        Set<String> missingWhitelistModules = []
        if (whitelist.size() != whitelistModules.size()) {
            Set<String> moduleNames = whitelistModules.collect { module ->
                module.attribute('name') as String
            }.toSet()
            missingWhitelistModules = whitelist - moduleNames
        }
        missingWhitelistModules
    }

    void run() {
        moduleWhitelist = veracodeSetup.moduleWhitelist
        Set<String> moduleIds = extractWhitelistModuleIds(XMLIO.readXml(preScanResultsOutputFile), moduleWhitelist)
        println "Module IDs: " + moduleIds.join(",")
        Node xml = XMLIO.writeXml(getOutputFile(), veracodeAPI.beginScan(app_id, moduleIds))
        VeracodeBuildInfo.printBuildInfo(xml)
        printf "report file: %s\n", getOutputFile()
    }
}
