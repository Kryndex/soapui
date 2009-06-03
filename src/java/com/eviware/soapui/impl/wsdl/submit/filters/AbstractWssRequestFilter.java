/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.submit.filters;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;

public abstract class AbstractWssRequestFilter extends AbstractRequestFilter
{
	public static final String WSS_DOC = "WsSecurityAuthenticationRequestFilter@Document";
	protected static DocumentBuilderFactory dbf;
	protected static DocumentBuilder db;

	static
	{
		dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating( false );
		dbf.setNamespaceAware( true );

		try
		{
			db = dbf.newDocumentBuilder();
		}
		catch( ParserConfigurationException e )
		{
			SoapUI.logError( e );
		}
	}

	protected static Document getWssDocument( SubmitContext context ) throws SAXException, IOException
	{
		String request = ( String )context.getProperty( BaseHttpRequestTransport.REQUEST_CONTENT );
		Document doc = ( Document )context.getProperty( WSS_DOC );

		// this should be solved with pooling for performance-reasons..
		if( doc == null || ((Integer)doc.getUserData( "requestContentHashCode" )).intValue() != request.hashCode() )
		{
			synchronized( db )
			{
				doc = db.parse( new InputSource( new StringReader( request ) ) );
				doc.setUserData( "requestContentHashCode", new Integer( request.hashCode()), null );
				context.setProperty( WSS_DOC, doc );
			}
		}
		
		return doc;
	}

	public void afterRequest( SubmitContext context, Response response )
	{
		context.removeProperty( WSS_DOC );
	}
}
