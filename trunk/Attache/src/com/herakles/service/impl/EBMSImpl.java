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
package com.herakles.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.herakles.jaxb.ebms.v3.CollaborationInfo;
import com.herakles.jaxb.ebms.v3.Envelope;
import com.herakles.jaxb.ebms.v3.From;
import com.herakles.jaxb.ebms.v3.Header;
import com.herakles.jaxb.ebms.v3.MessageInfo;
import com.herakles.jaxb.ebms.v3.Messaging;
import com.herakles.jaxb.ebms.v3.PartyInfo;
import com.herakles.jaxb.ebms.v3.PullRequest;
import com.herakles.jaxb.ebms.v3.Receipt;
import com.herakles.jaxb.ebms.v3.SignalMessage;
import com.herakles.jaxb.ebms.v3.To;
import com.herakles.jaxb.ebms.v3.UserMessage;
import com.herakles.service.api.EBMS;

/**
 * This is the API implementation.
 * http://www.mkyong.com/tutorials/jax-ws-tutorials/
 * 
 * @author yrajaram
 *
 */
@WebService(endpointInterface = "com.herakles.service.api.EBMS")
@Path("/ebms")
public class EBMSImpl implements EBMS, SOAPHandler<SOAPMessageContext>{
	@Resource
	WebServiceContext wsCtxt;

	private  WebServiceContext wsContext;
	private HttpHeaders	headers;
	
	private static JAXBContext CONTEXT = null;
	private String receivedUserMsgID = null,
			receivedPullMsgID = null,
					receivedMpc = null,
					receivedConversationId = null;
	private PartyInfo receivedUserMsgPartyInfo ;

	public EBMSImpl (@Context  WebServiceContext context, @Context HttpHeaders headers){
		super();
		this.wsContext = context;
		this.headers = headers;
	}
	public EBMSImpl (){
		super();
	}
	
	private JAXBContext getContext() {
		try {
			if (CONTEXT == null) {
				CONTEXT = JAXBContext.newInstance( "com.herakles.jaxb.ebms.v3" );
			}
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return CONTEXT;
	}
	
	public String pull(boolean debug) {
		String ret = "What the! "+debug;
		if (headers != null) {
			ret = "[Messaged:"+headers.getRequestHeader("MessageId").get(0)+"]";
			ret += "[timestamp:"+headers.getRequestHeader("timestamp").get(0)+"]";
			System.out.println("User agent: "+headers.getRequestHeader("user-agent").get(0));
		} else if (wsContext != null) {
			System.out.println("Ws Context:"+wsContext.toString());
		}
		
		if (debug){
			System.out.println("Enabling debug instrumentation");
			StringBuffer sb = new StringBuffer();
			
			if (headers != null){
				Map<String, Cookie> reqCookies = headers .getCookies();
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
				ret = sb.toString();
			}
		}
		getHttpHeaders();
		return ret;
	}
	
	public String push(boolean debug) {
		String ret = "got !";
		return ("PUSH "+ret +" and "+debug);
	}
	
	public Set<QName> getHeaders() {
        return new TreeSet();
    }

    public boolean handleMessage(SOAPMessageContext context) {
    	if (context == null) { // ReST req?
    		System.out.println("Not SOAP req? ReST?");
    	}
    	
        Boolean isResponse = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        SOAPMessage soapMsg = context.getMessage();
        SOAPEnvelope soapEnv = null;
        SOAPHeader soapHeader = null;
        
        if (isResponse) {
        	System.out.println("Outbound SOAP response");
            try {               
                soapEnv = soapMsg.getSOAPPart().getEnvelope();
                if ((soapHeader = soapEnv.getHeader())==null) {
                	System.out.println("Added header");
                	soapHeader = soapEnv.addHeader();
                }
                
                //-- Adding AS4 headers
                Marshaller m = getContext().createMarshaller();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.newDocument();
                doc.setXmlStandalone(false);
                
                Messaging msg = new Messaging();
                /*
                 * if request was user message and MEP was async (one way Push) then send out receipt ack
                 * if request was user message and MEP was Sync then get response and pack in user message and return
                 * if request was pull signal then get response or return 0006 signal
                 * if request was receipt  then process it
                 * if request was error then store - dont now how to handle for now
                 */
                UserMessage usrMsg = new UserMessage();
                SignalMessage signal = new SignalMessage();
                MessageInfo msgInfo = new MessageInfo();
                Receipt ack =  new Receipt();
                
    			boolean responseHasUserMsg = false;
    			if (responseHasUserMsg ) {
    				PartyInfo party = new PartyInfo();
    				From frm = new From();
    				frm.setRole(receivedUserMsgPartyInfo.getTo().getRole());
    				frm.getPartyId().add(receivedUserMsgPartyInfo.getTo().getPartyId().get(0));
    				party.setFrom(frm);
    				usrMsg.setPartyInfo(party);

    				To to = new To();
    				to.setRole(receivedUserMsgPartyInfo.getFrom().getRole());
    				to.getPartyId().add(receivedUserMsgPartyInfo.getFrom().getPartyId().get(0));
    				usrMsg.getPartyInfo().setTo(to);

    				CollaborationInfo collab = new CollaborationInfo();
    				collab.setConversationId(receivedConversationId);
    				usrMsg.setCollaborationInfo(collab);

    				msg.getUserMessage().add(usrMsg);
    			}
                
                msg.getSignalMessage().add(signal);
                msg.setMustUnderstand(true);
                
                signal.setReceipt(ack);
                signal.setMessageInfo(msgInfo);
                
                msgInfo.setMessageId("Ack Msg ID");
                msgInfo.setRefToMessageId(receivedUserMsgID);
                msgInfo.setTimestamp(getTimestamp());
                
//                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
//                m.marshal( msg, System.out );
                
                m.marshal( msg, doc );
                soapHeader.appendChild(soapHeader.getOwnerDocument().importNode(doc.getFirstChild(), true));
            } catch (Exception e) {
                System.out.println("Exception in handler: " + e);
                e.printStackTrace();
            }
        } else {
            // inbound
        	System.out.println("Inbound SOAP request");
        	
			try {
                soapEnv = soapMsg.getSOAPPart().getEnvelope();
                soapHeader = soapEnv.getHeader();
               
                Iterator it = soapHeader.extractHeaderElements(SOAPConstants.URI_SOAP_ACTOR_NEXT);

				System.out.println("Got ");
				soapMsg.writeTo(System.out);
				
				Unmarshaller u = getContext().createUnmarshaller();


				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				soapMsg.writeTo(baos);
				StringReader sr = new StringReader(new String(baos.toByteArray()));
		        final InputSource inputSource = new InputSource(new StringReader(new String(baos.toByteArray())));
//		        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//		        final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//		        final Document doc = dBuilder.parse(inputSource);
//		        doc.normalize();

		        JAXBElement<com.herakles.jaxb.ebms.v3.Envelope> je = (JAXBElement<Envelope>) u.unmarshal(sr);
		        com.herakles.jaxb.ebms.v3.Envelope ee = je.getValue();
		       
		        Header hh = ee.getHeader();

	        	JAXBElement<com.herakles.jaxb.ebms.v3.Messaging> jm = (JAXBElement<Messaging>) hh.getAny().get(0);
			    com.herakles.jaxb.ebms.v3.Messaging m = jm.getValue();
				for (int j=0; j<m.getSignalMessage().size();j++){
					//Handle signal message - put in DB
		
		        	// if PULL request then check DB for response
		        	PullRequest pullRequest = m.getSignalMessage().get(j).getPullRequest();
		        	if (pullRequest!=null){
			        	receivedPullMsgID = m.getSignalMessage().get(j).getMessageInfo().getMessageId();
			        	receivedMpc = pullRequest.getMpc();
				        System.out.println("Got PULL Msg:"+receivedPullMsgID);
		        	}
				}
				try {
	        	receivedUserMsgID = m.getUserMessage().get(0).getMessageInfo().getMessageId();	// there should be only one user msg
	        	receivedUserMsgPartyInfo = m.getUserMessage().get(0).getPartyInfo();
	        	receivedConversationId = m.getUserMessage().get(0).getCollaborationInfo().getConversationId();
				} catch (Exception e ) {
					// Just a kludge now to capture errors and to ignore - what if no user message comes in?
					System.out.println("-- Was here but why?"+e.getMessage());
				}
		        System.out.println("Got User Msg:"+receivedUserMsgID);
		        
			} catch (Exception e) {
				e.printStackTrace();
			} 
        }
        return true;
    }

	public boolean handleFault(SOAPMessageContext context) {
		System.out.println("ServerSOAPHandler.handleFault");
		boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outbound) {
			System.out.println("Direction=outbound (handleFault)");
			SOAPMessage msg = ((SOAPMessageContext) context).getMessage();
			// get SOAP-Part
			SOAPPart sp = msg.getSOAPPart();
			// edit Envelope
			SOAPEnvelope env;
			try {
				env = sp.getEnvelope();
				// add namespaces
				env.addNamespaceDeclaration("xsd",	"http://www.w3.org/2001/XMLSchema");
				env.addNamespaceDeclaration("xsi",	"http://www.w3.org/2001/XMLSchema-instance");
				env.addNamespaceDeclaration("soap",	"http://schemas.xmlsoap.org/soap/envelope");
				// get the SOAP-Body
				SOAPBody body = env.getBody();
				Iterator iter1 = body.getChildElements();
				while (iter1.hasNext()) {
					SOAPBodyElement bodyElement = (SOAPBodyElement) iter1.next();
					Iterator iter2 = bodyElement.getChildElements();
					while (iter2.hasNext()) {
						Object node = iter2.next();
						correctElement(node);
					}
				}
			} catch (SOAPException e) {
				e.printStackTrace();
			}
			dumpSOAPMessage(msg);
		} else {
			System.out.println("Direction=inbound (handleFault)");
		}
		SOAPMessage msg = ((SOAPMessageContext) context).getMessage();
		dumpSOAPMessage(msg);
		if (!outbound) {
			try {
				if (context.getMessage().getSOAPBody().getFault() != null) {
					String detailName = null;
					try {
						detailName = context.getMessage().getSOAPBody().getFault().getDetail().getFirstChild().getLocalName();
						System.out.println("detailName=" + detailName);
					} catch (Exception e) {
					}
				}
			} catch (SOAPException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

    public void close(MessageContext context) {
        // What to do here?!
    }

    public static XMLGregorianCalendar getTimestamp() {
    	XMLGregorianCalendar calendar = null;
		try {
			calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
    	return calendar;
    }
    
    private void generateSOAPErrMessage(SOAPMessage msg, String reason) {
        try {
           SOAPBody soapBody = msg.getSOAPPart().getEnvelope().getBody();
           SOAPFault soapFault = soapBody.addFault();
           soapFault.setFaultString(reason);
           throw new SOAPFaultException(soapFault); 
        }
        catch(SOAPException e) {
        	e.printStackTrace(); // what can we do with this!
        }
     }
    
    private void dumpSOAPMessage (SOAPMessage msg) {
		if (msg == null) {
			System.out.println("SOAP Message is null");
			return;
		}
		System.out.println("");
		System.out.println("--------------------");
		System.out.println("DUMP OF SOAP MESSAGE");
		System.out.println("--------------------");
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			msg.writeTo(baos);
			System.out.println(baos.toString(getMessageEncoding(msg)));
			// show included values
			String values = msg.getSOAPBody().getTextContent();
			System.out.println("Included values:" + values);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    private String getMessageEncoding (SOAPMessage msg) throws SOAPException {
		String encoding = "utf-8";
		if (msg.getProperty(SOAPMessage.CHARACTER_SET_ENCODING) != null) {
			encoding = msg.getProperty(SOAPMessage.CHARACTER_SET_ENCODING).toString();
		}
		return encoding;
	}

    private void correctElement (Object node) {
		System.out.println("Node: " + node);
		SOAPElement child;
		try {
			child = (SOAPElement) node;
		} catch (Exception e) {
			return;
		}
		String tagName = child.getTagName();
		if (tagMap.containsKey(tagName)) {
			try {
				child.addAttribute(new QName("http://www.w3.org/2001/XMLSchema-instance", "type", "xsi"), tagMap.get(tagName));
			} catch (SOAPException e) {
				e.printStackTrace();
			}
		}
		Iterator iter = child.getChildElements();
		while (iter.hasNext()) {
			node = iter.next();
			correctElement(node);
		}
	}
    
    static Map<String, String> tagMap;
    {
      tagMap = new HashMap ();
      tagMap.put ("errorCode", "xsd:string");
      tagMap.put ("faultstring", "xsd:string");
      tagMap.put ("ns4:fault", "ns4:MyException");
    }
   
    public String getHttpHeaders()   {
    	MessageContext context=wsCtxt.getMessageContext();
    	System.out.println("\n\n\t Got MessageContext context = "+context);
    	Map map = (Map)context.get(MessageContext.HTTP_REQUEST_HEADERS);
    	String str="";

    	Set entries = map.entrySet();
    	Iterator iterator = entries.iterator();
    	while (iterator.hasNext()) 	{
    		Map.Entry entry = (Map.Entry)iterator.next();
    		str=str+"\nHeader:" + entry.getKey() + " \tValue: " + entry.getValue();
    		System.out.println("\n\tHeader:" + entry.getKey() + " : " + entry.getValue());
    	}

    	System.out.println("\n\n\t\tClient's HTTP Headers are : "+str);
    	return "\n\t Your HTTP Headers are : "+str;
    }
}
