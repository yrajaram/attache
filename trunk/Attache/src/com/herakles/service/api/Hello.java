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

package com.herakles.service.api;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.herakles.service.domain.HelloResponse;

/**
 * The hello world services is used to perform actions on the data associated
 * with a greeting.
 * 
 * @author yrajaram
 */
@WebService
public interface Hello {
	@Path("/")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
	@WebMethod(action="getHelloWorld", operationName="getHelloWorld")
	HelloResponse getHelloWorld(
			@DefaultValue("false") @QueryParam("debug") boolean debug
			);

	@Path("/{name}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
	@WebMethod(action="getHello", operationName="getHello")
	HelloResponse getHello(
			@PathParam("name") String name,
			@DefaultValue("false") @QueryParam("debug") boolean debug
			);
}
