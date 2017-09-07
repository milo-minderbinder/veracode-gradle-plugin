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
        requiredArguments << 'app_id'
        dependsOn "veracodeGetPreScanResults"
        app_id = project.findProperty("app_id")
        defaultOutputFile = new File("${project.buildDir}/veracode", 'begin-scan.xml')
    }

    VeracodeGetPreScanResultsTask preScan = new VeracodeGetPreScanResultsTask()
    File preScanResultsOutputFile = preScan.getOutputFile()

    static Set<String> extractWhitelistModuleIds(Node xml, Set<String> whitelist) {
        Set<String> moduleIds = []
        Set<String> moduleNames = []
        NodeList moduleList = xml.getAt("module") as NodeList
        for (int i = 0; i < moduleList.size(); i++) {
            Node moduleEntry = moduleList.get(i) as Node
            String id = moduleEntry.attribute('id')
            String name = moduleEntry.attribute('name')
            String status = moduleEntry.attribute('status')
            if (!status.startsWith('(Fatal)') && whitelist.contains(name)) {
                moduleIds << id
                moduleNames << name
                printf "Selecting module: %s - %s\n", name, status
            }
        }
        if (whitelist.size() != moduleNames.size()) {
            Set<String> missingWhitelistModules = whitelist - moduleNames
            if (missingWhitelistModules.size() > 0) {
                // TODO: Look into logging levels. The whole plugin is using print statements.
                printf "WARNING: Missing whitelist modules: ${missingWhitelistModules}"
            }
        } else {
            printf "INFO: All whitelist modules found"
        }
        return moduleIds
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
        this.moduleWhitelist = veracodeSetup.moduleWhitelist
        Set<String> moduleIds = extractWhitelistModuleIds(readXml(preScanResultsOutputFile), moduleWhitelist)
        Node xml = writeXml(
                outputFile,
                uploadAPI().beginScan(
                        app_id,
                        moduleIds.join(","),
                        "", // scan_all_top_level_modules
                        "scan_selected_modules",
                        "") // scan_previously_selected_modules
        )
        printBeginScanStatus(xml)
    }
}
