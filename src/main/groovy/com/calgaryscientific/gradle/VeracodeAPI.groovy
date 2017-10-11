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

import com.veracode.apiwrapper.wrappers.ResultsAPIWrapper
import com.veracode.apiwrapper.wrappers.UploadAPIWrapper
import groovy.transform.CompileStatic
import org.gradle.api.GradleException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CompileStatic
class VeracodeAPI {
    private String username
    private String password
    private String key
    private String id
    private static Logger log = LoggerFactory.getLogger(VeracodeAPI.class);

    VeracodeAPI(String username, String password, String key, String id) {
        this.username = username
        this.password = password
        this.key = key
        this.id = id
    }

    String beginScan(String app_id, Set<String> moduleIds) {
        return uploadAPI().beginScan(
                app_id,
                moduleIds.join(","),
                "", // scan_all_top_level_modules
                "scan_selected_modules",
                "") // scan_previously_selected_modules
    }

    String getPreScanResults(String app_id) {
        return uploadAPI().getPreScanResults(app_id)
    }

    String getPreScanResults(String app_id, String build_id) {
        return uploadAPI().getPreScanResults(app_id, build_id)
    }

    String uploadFile(String app_id, String filePath) {
        return uploadAPI().uploadFile(app_id, filePath)
    }

    String detailedReport(String build_id) {
        return resultsAPI().detailedReport(build_id)
    }

    byte[] detailedReportPdf(String build_id) {
        return resultsAPI().detailedReportPdf(build_id)
    }

    String getCallStacks(String build_id, String flaw_id) {
        return resultsAPI().getCallStacks(build_id, flaw_id)
    }

    private boolean useAPICredentials() {
        if (username && password) {
            log.debug('Using username and password authentication')
            return false
        }
        if (!key && !id) {
            throw new GradleException("Missing Veracode Credentials!")
        }
        log.debug('Using key and ID authentication')
        return true
    }

    protected UploadAPIWrapper uploadAPI() {
        UploadAPIWrapper api = new UploadAPIWrapper()
        if (useAPICredentials()) {
            api.setUpApiCredentials(this.id, this.key)
        } else {
            api.setUpCredentials(this.username, this.password)
        }
        return api
    }

    protected ResultsAPIWrapper resultsAPI() {
        ResultsAPIWrapper api = new ResultsAPIWrapper()
        if (useAPICredentials()) {
            api.setUpApiCredentials(this.id, this.key)
        } else {
            api.setUpCredentials(this.username, this.password)
        }
        return api
    }
}
