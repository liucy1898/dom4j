/*
 * Copyright 2001 (C) MetaStuff, Ltd. All Rights Reserved.
 * 
 * This software is open source. 
 * See the bottom of this file for the licence.
 * 
 * $Id$
 */

package org.dom4j.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.ElementHandler;
import org.dom4j.DocumentException;
import org.dom4j.io.aelfred.SAXDriver;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/** <p><code>SAXReader</code> creates a DOM4J tree from SAX parsing events.</p>
  *
  * @author <a href="mailto:james.strachan@metastuff.com">James Strachan</a>
  * @version $Revision$
  */
public class SAXReader extends DocumentReader {

    /** <code>XMLReader</code> used to parse the SAX events */
    private XMLReader xmlReader;
    
    /** Whether validation should occur */
    private boolean validating;

    /** ElementHandler to call when each <code>Element</code> is complete */
    private ElementHandler elementHandler;
 
    /** ElementHandler to call before pruning occurs */
    private ElementHandler pruningElementHandler;
 
    /** ErrorHandler class to use */
    private ErrorHandler errorHandler;
 
    /** The pruning path to use if any */
    private String pruningPath;
 
    
    
    
    public SAXReader() {
    }

    public SAXReader(boolean validating) {
        this.validating = validating;
    }

    public SAXReader(XMLReader xmlReader) {
        this.xmlReader = xmlReader;
    }

    public SAXReader(XMLReader xmlReader, boolean validating) {
        this.xmlReader = xmlReader;
        this.validating = validating;
    }

    public SAXReader(String xmlReaderClassName) throws SAXException {
        if (xmlReaderClassName != null) {
            this.xmlReader = XMLReaderFactory.createXMLReader(xmlReaderClassName);
        }
    }
    
    public SAXReader(String xmlReaderClassName, boolean validating) throws SAXException {
        if (xmlReaderClassName != null) {
            this.xmlReader = XMLReaderFactory.createXMLReader(xmlReaderClassName);
        }
        this.validating = validating;
    }

    
    
    // Properties
    
    /** @return the validation mode, true if validating will be done 
      * otherwise false.
      */
    public boolean isValidating() {
        return validating;
    }
    
    /** Sets the validation mode.
      *
      * @param validating indicates whether or not validation should occur.
      */
    public void setValidation(boolean validating) {
        this.validating = validating;
    }


    /** @return the <code>ErrorHandler</code> used by SAX
      */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /** Sets the <code>ErrorHandler</code> used by the SAX 
      * <code>XMLReader</code>.
      *
      * @param errorHandler is the <code>ErrorHandler</code> used by SAX
      */
    public void setErrorHandler(ErrorHandler errorHandler) {
        errorHandler = errorHandler;
    }

    /** @return the <code>XMLReader</code> used to parse SAX events
      */
    public XMLReader getXMLReader() throws SAXException {
        if (xmlReader == null) {
            xmlReader = createXMLReader();
        }
        return xmlReader;
    }

    /** Sets the <code>XMLReader</code> used to parse SAX events
      *
      * @param xmlReader is the <code>XMLReader</code> to parse SAX events
      */
    public void setXMLReader(XMLReader xmlReader) {
        this.xmlReader = xmlReader;
    }

    /** Sets the class name of the <code>XMLReader</code> to be used 
      * to parse SAX events.
      *
      * @param xmlReaderClassName is the class name of the <code>XMLReader</code> 
      * to parse SAX events
      */
    public void setXMLReaderClassName(String xmlReaderClassName) throws SAXException {
        setXMLReader( XMLReaderFactory.createXMLReader(xmlReaderClassName) );
    }

    /** Returns the <code>ElementHandler</code> which is called as 
      * <code>Element</code> objects are built.
      *
      * @return the handler of elements 
      */
    public ElementHandler getElementHandler() {
        return elementHandler;
    }

    /** Sets the <code>ElementHandler</code> which is called as 
      * <code>Element</code> objects are built.
      *
      * @param elementHandler is the handler of elements 
      */
    public void setElementHandler(ElementHandler elementHandler) {
        this.elementHandler = elementHandler;
    }

    /** Returns the <code>ElementHandler</code> called when pruning 
      * large documents.
      *
      * @return the handler of elements which are about to be pruned
      */
    public ElementHandler getPruningElementHandler() {
        return pruningElementHandler;
    }

    /** Sets the <code>ElementHandler</code> called when pruning 
      * large documents.
      *
      * @param elementHandler is the handler of elements to use when pruning
      */
    public void getPruningElementHandler(ElementHandler pruningElementHandler) {
        this.pruningElementHandler = pruningElementHandler;
    }

    /** Returns the pruning patch used when parsing SAX events. The pruning path
      * allows large documents to be parsed.
      *
      * @return the pruningPath to use when reading documents
      */
    public String getPruningPath() {
        return pruningPath;
    }

    /** Sets the pruning patch used when parsing SAX events. The pruning path
      * allows large documents to be parsed.
      *
      * @param pruningPath is the pruningPath to use
      */
    public void setPruningPath(String pruningPath) {
        this.pruningPath = pruningPath;
    }

    /** Sets the pruning patch used when parsing SAX events. The pruning path
      * allows large documents to be parsed. It should contain the '/' character
      * to seperate paths such as "A/B/C".
      *
      * @param pruningPath is the path expression for the nodes to call the handler and to prune 
      */
    public void setPruningMode(String pruningPath, ElementHandler pruningElementHandler) {
        this.pruningPath = pruningPath;
        this.pruningElementHandler = pruningElementHandler;
    }
    

    
    
    // DocumentReader API
    
    /** <p>Reads a Document from the given <code>File</code></p>
      *
      * @param file is the <code>File</code> to read from.
      * @return the newly created Document instance
      * @throws DocumentException if an error occurs during parsing.
      * @throws FileNotFoundException if the file could not be found
      */
    public Document read(File file) throws DocumentException, FileNotFoundException {
        Document document = read(new BufferedReader(new FileReader(file)));
        //Document document = read(file.getAbsolutePath());
        document.setName( file.getAbsolutePath() );
        return document;
    }
    
    /** <p>Reads a Document from the given <code>URL</code> using SAX</p>
      *
      * @param url <code>URL</code> to read from.
      * @return the newly created Document instance
      * @throws DocumentException if an error occurs during parsing.
      */
    public Document read(URL url) throws DocumentException {
        String systemID = url.toExternalForm();
        Document document = read(new InputSource(systemID));
        document.setName( url.toString() );
        return document;
    }
    
    /** <p>Reads a Document from the given URI using SAX</p>
      *
      * @param systemId is the URI for the input
      * @return the newly created Document instance
      * @throws DocumentException if an error occurs during parsing.
      */
    public Document read(String systemId) throws DocumentException {
        return read(new InputSource(systemId));
    }

    /** <p>Reads a Document from the given stream using SAX</p>
      *
      * @param in <code>InputStream</code> to read from.
      * @return the newly created Document instance
      * @throws DocumentException if an error occurs during parsing.
      */
    public Document read(InputStream in) throws DocumentException {
        return read(new InputSource(in));
    }

    /** <p>Reads a Document from the given <code>Reader</code> using SAX</p>
      *
      * @param reader is the reader for the input
      * @return the newly created Document instance
      * @throws DocumentException if an error occurs during parsing.
      */
    public Document read(Reader reader) throws DocumentException {
        return read(new InputSource(reader));
    }

    /** <p>Reads a Document from the given stream using SAX</p>
      *
      * @param in <code>InputStream</code> to read from.
      * @param systemId is the URI for the input
      * @return the newly created Document instance
      * @throws DocumentException if an error occurs during parsing.
      */
    public Document read(InputStream in, String systemId) throws DocumentException {
        InputSource source = new InputSource(in);
        source.setSystemId(systemId);
        return read(source);
    }

    /** <p>Reads a Document from the given <code>Reader</code> using SAX</p>
      *
      * @param reader is the reader for the input
      * @param systemId is the URI for the input
      * @return the newly created Document instance
      * @throws DocumentException if an error occurs during parsing.
      */
    public Document read(Reader reader, String SystemId) throws DocumentException {
        InputSource source = new InputSource(reader);
        source.setSystemId(SystemId);
        return read(source);
    }
    
    /** <p>Reads a Document from the given <code>InputSource</code> using SAX</p>
      *
      * @param in <code>InputSource</code> to read from.
      * @param systemId is the URI for the input
      * @return the newly created Document instance
      * @throws DocumentException if an error occurs during parsing.
      */
    public Document read(InputSource in) throws DocumentException {
        try {
            XMLReader reader = getXMLReader();

            Document document = createDocument();
            DefaultHandler contentHandler = createContentHandler(document);
            reader.setContentHandler(contentHandler);

            configureReader(reader, contentHandler);
        
            reader.parse(in);
            return document;
        } 
        catch (Exception e) {
            if (e instanceof SAXParseException) {
                SAXParseException parseException = (SAXParseException) e;
                String systemId = parseException.getSystemId();
                if ( systemId == null ) {
                    systemId = "";
                }
                String message = "Error on line " 
                    + parseException.getLineNumber()
                    + " of document "  + systemId
                    + " : " + parseException.getMessage();
                
                throw new DocumentException(message, e);
            }
            else {
                throw new DocumentException(e.getMessage(), e);
            }
        }
    }
    

    
    // Implementation methods    
    
    protected void configureReader(XMLReader reader, DefaultHandler contentHandler) throws DocumentException {                
        // configure lexical handling
        setParserProperty(
            reader,
            "http://xml.org/sax/handlers/LexicalHandler", 
            contentHandler
        );
        // try alternate property just in case
        setParserProperty(
            reader,
            "http://xml.org/sax/properties/lexical-handler", 
            contentHandler
        );
        
        try {
            // configure namespace support
            reader.setFeature(
                "http://xml.org/sax/features/namespaces", 
                true
            );
            reader.setFeature(
                "http://xml.org/sax/features/namespace-prefixes", 
                false
            );

            // configure validation support
            reader.setFeature(
                "http://xml.org/sax/features/validation", 
                isValidating()
            );
            if (errorHandler != null) {
                 reader.setErrorHandler(errorHandler);
            }
            else {
                 reader.setErrorHandler(contentHandler);
            }
        } 
        catch (Exception e) {
            if (isValidating()) {
                throw new DocumentException( 
                    "Validation not supported for XMLReader: " + reader, 
                    e
                );
            }

        }
    }
        
    protected boolean setParserProperty(XMLReader reader, String propertyName, ContentHandler contentHandler) {    
        try {
            reader.setProperty(propertyName, contentHandler);
            return true;
        } 
        catch (SAXNotSupportedException e) {
        } 
        catch (SAXNotRecognizedException e) {
        }
        return false;
    }

    /** Factory Method to allow user derived SAXContentHandler objects to be used
      */
    protected DefaultHandler createContentHandler( Document document ) {
        if ( pruningPath != null ) {
            // NOTE: should handle full XPath patterns sometime
            String[] path = tokenizePath( pruningPath );
            ElementStack stack = new PruningElementStack( path, getPruningElementHandler() );
            return new SAXContentHandler(document, getElementHandler(), stack);
        }
        else {
            return new SAXContentHandler(document, getElementHandler());
        }
    }
    
    protected String[] tokenizePath(String path) {
        ArrayList list = new ArrayList();
        if ( path.charAt(0) == '/' ) {
            path = path.substring(1);
        }
        for ( StringTokenizer enum = new StringTokenizer( path, "/" ); enum.hasMoreTokens(); ) {
            list.add( enum.nextToken() );
        }
        String[] answer = new String[ list.size() ];
        list.toArray( answer );
        return answer;
    }

    /** Factory Method to allow alternate methods of 
      * creating and configuring XMLReader objects
      */
    protected XMLReader createXMLReader() throws SAXException {
        String className = System.getProperty( "org.xml.sax.driver" );
        if (className == null || className.trim().length() <= 0) {
            return new SAXDriver();
        }
        return XMLReaderFactory.createXMLReader();
    }


}




/*
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "DOM4J" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of MetaStuff, Ltd.  For written permission,
 *    please contact dom4j-info@metastuff.com.
 *
 * 4. Products derived from this Software may not be called "DOM4J"
 *    nor may "DOM4J" appear in their names without prior written
 *    permission of MetaStuff, Ltd. DOM4J is a registered
 *    trademark of MetaStuff, Ltd.
 *
 * 5. Due credit should be given to the DOM4J Project
 *    (http://dom4j.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY METASTUFF, LTD. AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * METASTUFF, LTD. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * $Id$
 */
