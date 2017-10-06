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

import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import groovy.transform.CompileStatic

@CompileStatic
class VeracodeUploadFileTask extends VeracodeTask {
    static final String NAME = 'veracodeUploadFile'
    private String app_id
    String maxUploadAttempts
    String waitTimeBetweenAttempts

    VeracodeUploadFileTask() {
        description = "Uploads all files defined in 'filesToUpload' to Veracode based on the given 'app_id'"
        requiredArguments << 'app_id'
        optionalArguments << 'maxUploadAttempts' << 'waitTimeBetweenAttempts'
        app_id = project.findProperty("app_id")
        maxUploadAttempts = project.findProperty("maxUploadAttempts")
        waitTimeBetweenAttempts = project.findProperty("waitTimeBetweenAttempts")
        defaultOutputFile = new File("${project.buildDir}/veracode", "upload-file-latest.xml")
    }

    @OutputFile
    File getOutputFile() {
        return defaultOutputFile
    }

    @InputFiles
    Set<File> getFileSet() {
        veracodeSetup = project.findProperty("veracodeSetup") as VeracodeSetup
        Set<File> fc = veracodeSetup.filesToUpload
        return fc
    }

    static void printFileUploadStatus(Node xml) {
        NodeList fileList = xml.getAt("file") as NodeList
        for (int i = 0; i < fileList.size(); i++) {
            Node fileEntry = fileList.get(i) as Node
            println "${fileEntry.attribute('file_name')}=${fileEntry.attribute('file_status')}"
        }
    }

    void uploadFile(File file, Integer maxTries, Integer waitTime) {
        Exception error
        Integer tries = 1;
        println "Processing ${file.name}"

        boolean success = false
        while (!success && (tries <= maxTries || maxTries == 0)) {
            if (tries > 1) {
                println "Attempt ${tries}"
                println "Maximum upload attempts = ${maxTries} (0 means keep trying)"
            }
            try {
                String response = veracodeAPI.uploadFile(app_id, file.absolutePath)
                Node xml = XMLIO.writeXml(getOutputFile(), response)
                printFileUploadStatus(xml)
                success = true
            } catch (Exception e) {
                println ''
                println e
                println ''
                error = e
                sleep(waitTime)
                tries++
            }
        }
        if (tries > maxTries) {
            throw error
        }
    }


    void run() {
        Integer maxTries = Integer.parseInt((this.maxUploadAttempts != null) ? this.maxUploadAttempts : '10')
        Integer waitTime = Integer.parseInt((this.waitTimeBetweenAttempts != null) ? this.waitTimeBetweenAttempts : '5000')
        println "results file: ${getOutputFile()}"
        println ''
        for (File file : getFileSet()) {
            uploadFile(file, maxTries, waitTime)
        }
    }
}
