/*
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 *  Copyright 2012.
 *  All rights reserved.
 *
 * 
 *  Unless agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.herakles.service.domain;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The response object that will be returned to the caller.
 * 
 * @author yrajaram
 */
@XmlRootElement
public class HelloResponse{
	
	private String response, debugInfo;

	public String getResponse() {
		return response;
	}

	public void setResponse(String name) {
		this.response = name;
	}

	public String getDebugInfo() {
		return debugInfo;
	}

	public void setDebugInfo(String debugInfo) {
		this.debugInfo = debugInfo;
	}
}
