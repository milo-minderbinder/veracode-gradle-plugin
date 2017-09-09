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
        description = 'Starts a Veracode scan for given application ID'
        requiredArguments << 'app_id' << 'build_id'
        dependsOn "veracodeGetPreScanResults"
        app_id = project.findProperty("app_id")
        defaultOutputFile = new File("${project.buildDir}/veracode", 'begin-scan.xml')
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
        NodeList moduleList = xml.getAt("module") as NodeList
        HashMap<String, List<String>> nonFatalModules = filterOutFatalModules(moduleList)
        HashMap<String, List<String>> whitelistModules = getWhitelistModules(nonFatalModules, whitelist)
        Set<String> missingWhitelistModules = getMissingWhitelistModules(whitelist, whitelistModules)
        if (missingWhitelistModules.size() > 0) {
            // TODO: Look into logging levels. The whole plugin is using print statements.
            printf "WARNING: Missing whitelist modules: ${missingWhitelistModules}\n"
        }
        Set<String> moduleIds = []
        for (List<String> data : whitelistModules.values()) {
            moduleIds << data.get(0)
        }
        return moduleIds
    }

    /**
     * Given a Veracode moduleList NodeList (xml), it will return a Map of names to [id, status] of modules that are
     * not fatal.
     *
     * @param moduleList
     * @return Map{ name: [id, status] }
     */
    private static HashMap<String, List<String>> filterOutFatalModules(NodeList moduleList) {
        HashMap<String, List<String>> nonFatalModules = new HashMap()
        for (int i = 0; i < moduleList.size(); i++) {
            Node moduleEntry = moduleList.get(i) as Node
            String id = moduleEntry.attribute('id')
            String name = moduleEntry.attribute('name')
            String status = moduleEntry.attribute('status')
            if (!status.startsWith('(Fatal)')) {
                List<String> data = [id, status]
                nonFatalModules.put(name, data)
            }
        }
        return nonFatalModules
    }

    /**
     * Given a list of modules, it will return the subset of modules that are found in the whitelist.
     * @param modules
     * @param whitelist
     * @return Map{ name: [id, status] }
     */
    private
    static HashMap<String, List<String>> getWhitelistModules(HashMap<String, List<String>> modules, Set<String> whitelist) {
        HashMap<String, List<String>> whitelistedModules = new HashMap()
        for (Map.Entry<String, List<String>> module : modules.entrySet()) {
            String name = module.getKey()
            List<String> data = module.getValue()
            String id = data.get(0)
            String status = data.get(1)
            if (whitelist.contains(name)) {
                whitelistedModules.put(id, data)
                printf "Selecting module: %s: %s - %s\n", id, name, status
            }
        }
        whitelistedModules
    }

    /**
     * Given a whitelist and a list of whitelistModules,
     * it will return the whitelist entries that aren't part of the whitelist modules.
     * @param whitelist
     * @param whitelistModules
     * @return
     */
    private
    static Set<String> getMissingWhitelistModules(Set<String> whitelist, HashMap<String, List<String>> whitelistModules) {
        Set<String> missingWhitelistModules = []
        if (whitelist.size() != whitelistModules.size()) {
            missingWhitelistModules = whitelist - whitelistModules.keySet()
        }
        missingWhitelistModules
    }


    static void printBeginScanStatus(Node xml) {
        String app_id = xml.attribute('app_id')
        String build_id = xml.attribute('build_id')
        NodeList buildList = xml.getAt("build") as NodeList
        Node build = buildList.get(0) as Node
        String version = build.attribute('version')
        NodeList analysis_unitList = build.getAt('analysis_unit') as NodeList
        Node analysis_unit = analysis_unitList.get(0) as Node
        String analysis_type = analysis_unit.attribute('analysis_type')
        String status = analysis_unit.attribute('status')
        printf "app_id=%s build_id=%s version=%s analysis_type=%s status=%s\n",
                app_id, build_id, version, analysis_type, status
    }

    void run() {
        moduleWhitelist = veracodeSetup.moduleWhitelist
        Set<String> moduleIds = extractWhitelistModuleIds(readXml(preScanResultsOutputFile), moduleWhitelist)
        println "Module IDs: " + moduleIds.join(",")
        Node xml = writeXml(outputFile, veracodeAPI.beginScan(app_id, moduleIds))
        printBeginScanStatus(xml)
    }
}
