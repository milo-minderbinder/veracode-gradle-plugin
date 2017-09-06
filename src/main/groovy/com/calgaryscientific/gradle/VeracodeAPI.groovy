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

@CompileStatic
class VeracodeAPI {
    private String username
    private String password
    private String key
    private String id

    VeracodeAPI(String username, String password, String key, String id) {
        this.username = username
        this.password = password
        this.key = key
        this.id = id
    }

    String getPreScanResults(String app_id) {
        UploadAPIWrapper api = uploadAPI()
        return uploadAPI().getPreScanResults(app_id)
    }

    String getPreScanResults(String app_id, String build_id) {
        UploadAPIWrapper api = uploadAPI()
        return uploadAPI().getPreScanResults(app_id, build_id)
    }

    String uploadFile(String app_id, String filePath) {
        UploadAPIWrapper api = uploadAPI()
        return api.uploadFile(app_id, filePath)
    }

    private boolean useAPICredentials() {
        if (this.username != "" && this.password != "") {
            return false
        }
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
