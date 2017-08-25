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

class VeracodeCreateBuildTask extends VeracodeTask {
    static final String NAME = 'veracodeCreateBuild'

    VeracodeCreateBuildTask() {
        description = 'Creates a new build under the application id passed in, with the buildName as the identifier for the new build'
        requiredArguments << 'appId' << 'buildName'
    }

    void run() {
        Node buildInfo = writeXml(
                'build/create-build.xml',
                uploadAPI().createBuild(project.appId, project.buildName))
        if (buildInfo.name().equals('error')) {
            println buildInfo.text()
        } else {
            println '[Build]'
            buildInfo.build[0].attributes().each() { k, v ->
                println "\t$k=$v"
            }
            println '[Analysis Unit]'
            buildInfo.build[0].children()[0].attributes().each { k, v ->
                println "\t$k=$v"
            }
        }
    }
}