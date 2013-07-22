/*
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 *  Copyright 2012.
 *  All rights reserved.
 *

 *  Unless agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.herakles.service.impl;

import java.util.Map;

import javax.jws.WebService;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import com.herakles.service.api.Hello;
import com.herakles.service.domain.HelloResponse;

/**
 * This is the API implementation.
 * 
 * @author yrajaram
 *
 */
@WebService(endpointInterface = "com.herakles.service.api.Hello")
@Path("/hello")
public class HelloImpl implements Hello{
	private HttpHeaders headers;
	
	public HelloImpl (@Context HttpHeaders headers){
		super();
		this.headers = headers;
	}
	public HelloImpl (){
		super();
	}
	public HelloResponse getHello(String name, boolean debug) {
		
		if (name==null || "".equals(name)) name="World";
		
		HelloResponse ret = new HelloResponse();
		ret.setResponse("Hello "+name);
		
		if (debug){
			System.out.println("Enabling debug instrumentation");
			StringBuffer sb = new StringBuffer();
			
			Map<String, Cookie> reqCookies = headers.getCookies();
			for (String key : reqCookies.keySet()) {
				sb.append("[");
				sb.append(key);
				sb.append("=");
				sb.append(reqCookies.get(key));
				sb.append("]");
			}
			
			MultivaluedMap<String, String> reqHeaders = headers.getRequestHeaders();
			for (String key : reqHeaders.keySet()) {
				sb.append("[");
				sb.append(key);
				sb.append("=");
				sb.append(reqHeaders.getFirst(key));
				sb.append("]");
			}
			ret.setDebugInfo(sb.toString());
		}
		return ret;
	}

	public HelloResponse getHelloWorld(boolean debug) {
		return getHello("", debug);
	}
}
