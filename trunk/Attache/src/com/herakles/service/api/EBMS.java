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
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * The EBMS services to perform PUSH and PULL
 * 
 * @author yrajaram
 * 
 * //			@WebParam(header = true, mode = Mode.IN) Messaging as4, 
 */
@WebService
public interface EBMS {
	@Path("/")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
	@WebMethod (action="pull", operationName="pull")
	String pull(
			@DefaultValue("false") @QueryParam("debug") boolean debug
			);

	@Path("/")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
	@WebMethod (action="push", operationName="push")
	String push(
			@DefaultValue("false") @QueryParam("debug") boolean debug
			);
}