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
class VeracodeVerifyMitigationsTask extends VeracodeTask {
    static final String NAME = 'veracodeVerifyMitigations'
    private String build_id
    HashMap<String, String> repoPathMap
    HashMap<String, List<File>> repoFilesMap

    VeracodeVerifyMitigationsTask() {
        description = "Verify the proposed mitigations from the detailed report based on the given 'build_id'"
        requiredArguments << 'build_id'
//        dependsOn 'veracodeDetailedReport'
        build_id = project.findProperty("build_id")
    }

    String getFileLine(String filename, Integer n) {
        FileInputStream fstream = new FileInputStream(filename)
        DataInputStream dstream = new DataInputStream(fstream)
        BufferedReader br = new BufferedReader(new InputStreamReader(dstream))
        String line;
        Integer number = 0
        while ((line = br.readLine()) != null) {
            number++
            if (number == n) {
                break
            }
        }
        dstream.close();
        return line
    }

    /**
     * Get all files from a directory recursively
     * @param dirname
     */
    List<File> getRecursiveFileList(String dirname) {
        List<File> files = []
        File dir = new File(dirname)
        for (File file : dir.listFiles() as List<File>) {
            if (file.isFile()) {
                files.add(file)
            } else if (file.isDirectory()) {
                files.addAll(getRecursiveFileList(file.getAbsolutePath()))
            }
        }
        return files
    }

    HashMap<String, List<File>> getRepoFiles(HashMap<String, String> repoPathMap) {
        HashMap<String, List<File>> repoFilesMap = new HashMap<>()
        repoPathMap.each {
            List<File> repoFiles = getRecursiveFileList(it.value)
            repoFilesMap.put(it.key, repoFiles)
        }
        return repoFilesMap
    }

    static List<File> findFileInRepo(HashMap<String, List<File>> repos, String repo, String filename) {
        return repos[repo].findAll { it.name == filename }
    }

    static List<File> findFileInRepo(List<File> repoFiles, String filename) {
        return repoFiles.findAll { it.name == filename }
    }

    void run() {
    }
}
