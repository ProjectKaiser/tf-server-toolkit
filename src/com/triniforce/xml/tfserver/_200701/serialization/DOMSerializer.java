/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.xml.tfserver._200701.serialization;


import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.triniforce.server.soap.CLOBValue;
import com.triniforce.server.soap.NamedVar;
import com.triniforce.server.soap.VObject;
import com.triniforce.server.soap.VUnknown;
import com.triniforce.utils.ApiAlgs;

public class DOMSerializer implements NamespaceContext{

    public static class EUnknownSerializationType extends RuntimeException {
        private static final long serialVersionUID = -390367859513513706L;
        public EUnknownSerializationType(String msg) {
            super(msg);
        }
    }
    
    public static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema"; //$NON-NLS-1$
    public static final String AXIS_NAMESPACE = "http://xml.apache.org/axis/"; //$NON-NLS-1$
    
    public final static String WSDL_PATH = "folkfolder.wsdl"; //$NON-NLS-1$
    public static final String TF_TDS_NAMESPACE = "http://www.triniforce.com/xml/folkfolder/soap/ver200701"; //$NON-NLS-1$
    public static final String TF_SOAP_NAMESPACE = "http://www.triniforce.com/xml/folkfolder/soap/ver200701"; //$NON-NLS-1$

    public static final String MOD_SESSIONID_TAG = "sessionId"; //$NON-NLS-1$
    public static final String MOD_FILEID_TAG = "fileId"; //$NON-NLS-1$
    public static final String MOD_FILETYPE_TAG = "fileType"; //$NON-NLS-1$
    public static final String MOD_OPERATIONS_TAG = "op"; //$NON-NLS-1$
    public static final String MOD_HEADER_TAG = "header"; //$NON-NLS-1$
    public static final String OPINSERT_TAG = "opInsert"; //$NON-NLS-1$
    public static final String OPUPDATE_TAG = "opUpdate"; //$NON-NLS-1$
    public static final String OPDELETE_TAG = "opDelete"; //$NON-NLS-1$
    public static final String OP_TABLENAME_TAG = "tableName"; //$NON-NLS-1$
    public static final String OP_ROWID_TAG = "rowId"; //$NON-NLS-1$
    public static final String OP_PARAM_TAG = "param"; //$NON-NLS-1$
    public static final String PROPVALUE_NAME_TAG = "name"; //$NON-NLS-1$
    public static final String PROPVALUE_VALUE_TAG = "value"; //$NON-NLS-1$
    public static final String VARIANT_VALUE_ATTR_TAG = "value"; //$NON-NLS-1$
    public static final String VARIANT_VINT_TAG = "vInt"; //$NON-NLS-1$
    public static final String VARIANT_VSTRING_TAG = "vString"; //$NON-NLS-1$
    public static final String VARIANT_VDATETIME_TAG = "vDateTime"; //$NON-NLS-1$
    public static final String VARIANT_VNULL_TAG = "vNull";     //$NON-NLS-1$
    public static final String VARIANT_VARRAY_TAG = "vArray"; //$NON-NLS-1$
    public static final String VARIANT_VLONG_TAG = "vLong"; //$NON-NLS-1$  
    public static final String VARIANT_VDOUBLE_TAG = "vDouble"; //$NON-NLS-1$
    public static final String VARIANT_VDECIMAL_TAG = "vDecimal"; //$NON-NLS-1$
    public static final String VARIANT_VNAMEDVAR_TAG = "vNamedVar"; //$NON-NLS-1$
    public static final String VARRAY_VARIANT_TAG = "vVariant";     //$NON-NLS-1$
    public static final String VNAMEDVAR_NAME_TAG = "name"; //$NON-NLS-1$
    public static final String VNAMEDVAR_VALUE_TAG = "value"; //$NON-NLS-1$
    public static final String FILTERNAME_TAG = "filterName"; //$NON-NLS-1$
    public static final String FILTERPROPS_TAG = "prop"; //$NON-NLS-1$
    public static final String PROPERTY_OBJNAME_TAG = "objName"; //$NON-NLS-1$
    public static final String PROPERTY_PROPNAME_TAG = "propName"; //$NON-NLS-1$
    public static final String SELECT_SESSIONID_TAG = "sessionId"; //$NON-NLS-1$
    public static final String SELECT_FROM_TAG = "from"; //$NON-NLS-1$
    public static final String SELECT_COLUMNS_TAG = "column"; //$NON-NLS-1$
    public static final String SELECT_PARAMS_TAG = "param"; //$NON-NLS-1$
    public static final String SELECT_ORDER_TAG = "order"; //$NON-NLS-1$
    public static final String SELECT_FILTERS_TAG = "filter"; //$NON-NLS-1$
    public static final String SELECT_STARTFROM_TAG = "startFrom"; //$NON-NLS-1$
    public static final String SELECT_LIMIT_TAG = "limit"; //$NON-NLS-1$
    public static final String SELECT_HEADER_TAG = "header"; //$NON-NLS-1$
    public static final String LOGINREQUEST_LOGIN_TAG = "login"; //$NON-NLS-1$
    public static final String LOGINREQUEST_PASSWORD_TAG = "password"; //$NON-NLS-1$
    public static final String SELECTRESULT_FIELDDEF_TAG = "fieldDef"; //$NON-NLS-1$
    public static final String SELECTRESULT_VALUES_TAG = "value"; //$NON-NLS-1$
    public static final String SELECTRESULT_HEADER_TAG = "header"; //$NON-NLS-1$
    public static final String FIELDDEF_NAME_TAG = "name"; //$NON-NLS-1$
    public static final String MODRESP_IDMAP_TAG = "idMap"; //$NON-NLS-1$
    public static final String MODRESP_HEADER_TAG = "header"; //$NON-NLS-1$
    public static final String IDMAP_CLIENTID_TAG = "clientId"; //$NON-NLS-1$
    public static final String IDMAP_SERVERID_TAG = "serverId"; //$NON-NLS-1$
    public static final String SELECT_TAG = "Select"; //$NON-NLS-1$
    public static final String SELECT_RESULT_TAG = "OutSelectResponse"; //$NON-NLS-1$
    public static final String MODIFICATION_TAG = "Modification"; //$NON-NLS-1$
    public static final String MODIFIACTION_RESPONSE_TAG = "OutModificationResponse"; //$NON-NLS-1$
    private static final String VARIANT_VCLOB_TAG = "vCLOB"; //$NON-NLS-1$
    private static final String VCLOB_MIME_TAG = "mimeType"; //$NON-NLS-1$
    private static final String VCLOB_DATA_TAG = "data"; //$NON-NLS-1$
    private static final String VCLOB_CREATORID_TAG = "creatorId"; //$NON-NLS-1$
    private static final String VCLOB_CREATORNICK_TAG = "creatorNickName"; //$NON-NLS-1$
    private static final String VCLOB_CREATED_TAG = "created"; //$NON-NLS-1$
    /*
    private static final String EGENERAL_ERROR_TAG = "EGeneralServerError"; //$NON-NLS-1$
    private static final String EAUTH_TAG = "EAuth"; //$NON-NLS-1$
    private static final String AXIS_EXCEPTION_NAME_TAG = "exceptionName"; //$NON-NLS-1$
    private static final String AXIS_HOSTNAME_TAG = "hostName"; //$NON-NLS-1$
    private static final String DEFAULT_HOSTNAME = "unknown"; //$NON-NLS-1$
    private static final String EGENERROR_MESSAGE_TAG = "message"; //$NON-NLS-1$
    private static final String EGENERROR_EXCNAME_TAG = "nativeExceptionName"; //$NON-NLS-1$
    private static final String EGENERROR_LOCATION_TAG = "location"; //$NON-NLS-1$
    private static final String EGENERROR_STACK_TAG = "stack"; //$NON-NLS-1$
    private static final String EGENERROR_ANCESTOR_TAG = "ancestor"; //$NON-NLS-1$
    */
    private static final String VARIANT_VOBJECT_TAG = "vObject"; //$NON-NLS-1$
    private static final String VOBJECT_TYPE_TAG = "type"; //$NON-NLS-1$
    private static final String VOBJECT_PROP_TAG = "prop"; //$NON-NLS-1$
    private static final String VARIANT_VUNKNOWN_TAG = "vUnknown"; //$NON-NLS-1$
    
    private static final String[] VAL_TYPES = {VARIANT_VARRAY_TAG, 
        VARIANT_VCLOB_TAG,
        VARIANT_VDATETIME_TAG, 
        VARIANT_VDECIMAL_TAG, 
        VARIANT_VDOUBLE_TAG,                                    
        VARIANT_VINT_TAG, 
        VARIANT_VLONG_TAG,
        VARIANT_VNAMEDVAR_TAG,
        VARIANT_VNULL_TAG, 
        VARIANT_VOBJECT_TAG,
        VARIANT_VSTRING_TAG,
        VARIANT_VUNKNOWN_TAG
    };
    
    public enum SUPPORTED_TYPES{INT, STRING, DATETIME};    
    
    public static DatatypeFactory TYPE_FACTORY;
    
    public static TransformerFactory       TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    public static DocumentBuilderFactory   DOCBUILDER_FACTORY =  DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", DOMSerializer.class.getClassLoader());
    public static SchemaFactory            SCHEMA_FACTORY = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    public static XPathFactory             XPATH_FACTORY = XPathFactory.newInstance();

    static{
        
        try {
            TYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
            ApiAlgs.rethrowException(e);
        }
        /*
        WSDL_PATH = "folkfolder.wsdl";
        
        InputSource wsdlSource = new InputSource(SelectRequest.class.getResourceAsStream(WSDL_PATH));
        XPath xp = XPATH_FACTORY.newXPath();
        try {
            Element node = (Element)xp.evaluate("/wsdl:definitions", wsdlSource, XPathConstants.NODE);
            TF_SOAP_NAMESPACE = node.getAttribute("targetNamespace");
            node = (Element)xp.evaluate("/wsdl:definitions/wsdl:types/xsd:schema", wsdlSource, XPathConstants.NODE);
            TF_TDS_NAMESPACE = node.getAttribute("targetNamespace");        
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }*/
    }
    
    public DOMSerializer(){
        
    }
    
    public Object createVariantObject(Element variantNode) throws DOMException, ParseException, XPathExpressionException{

        Object result=null;
        
        switch(Arrays.binarySearch(VAL_TYPES, variantNode.getLocalName())){
        case 0: //array
            NodeList nodes = variantNode.getElementsByTagName(VARRAY_VARIANT_TAG);
            Object[] resultArray = new Object[nodes.getLength()];
            for (int i = 0; i < nodes.getLength(); i++) {
                resultArray[i] = createVariantObject(getFirstChildElement((Element)nodes.item(i)));
            }
            result = resultArray;
            break;
        case 1: //CLOB
            String mime = ((Element)variantNode.getElementsByTagName(VCLOB_MIME_TAG).item(0)).getTextContent();
            String value = ((Element)variantNode.getElementsByTagName(VCLOB_DATA_TAG).item(0)).getTextContent();            
            result = new CLOBValue(mime, value);
            break;
        case 2: //datetime
            result = TYPE_FACTORY.newXMLGregorianCalendar(variantNode.getAttribute(VARIANT_VALUE_ATTR_TAG));
            break;
        case 3: //decimal
            result = new BigDecimal(variantNode.getAttribute(VARIANT_VALUE_ATTR_TAG));
            break;
        case 4: //double
            result = Double.parseDouble(variantNode.getAttribute(VARIANT_VALUE_ATTR_TAG));
            break;            
        case 5: //integer
            result = Integer.decode(variantNode.getAttribute(VARIANT_VALUE_ATTR_TAG));
            break;
        case 6: //long
            result = Long.decode(variantNode.getAttribute(VARIANT_VALUE_ATTR_TAG));
            break;
        case 7: //named var
            result = createNamedVar(variantNode);
            break;
        case 8: //null
            result = null;
            break;
        case 9: //object
            result = createVObject(variantNode);
            break;
        case 10: //string
            result = variantNode.getAttribute(VARIANT_VALUE_ATTR_TAG);
            break;
        case 11: //unknown
            result = new VUnknown();
        }
        
        return result;        
    }
    
    private static Element getFirstChildElement(Element element) {
        NodeList childs = element.getChildNodes(); 
        for (int i = 0; i <childs.getLength(); i++) {
            Node res = childs.item(i);
            if(res instanceof Element)
                return (Element) res; 
        }
        return null;
    }

    public VObject createVObject(Element vObjElement) throws DOMException, ParseException, XPathExpressionException{
        String type = vObjElement.getAttribute(VOBJECT_TYPE_TAG);
        
        NodeList childs = vObjElement.getChildNodes();
        ArrayList<NamedVar> vals = new ArrayList<NamedVar>();
        for(int i=0; i<childs.getLength(); i++){
            Node child = childs.item(i);
                if(child instanceof Element && child.getNodeName().equals(VOBJECT_PROP_TAG)){
                    vals.add(createNamedVar((Element) child));
                }
        }
        return new VObject(type, vals.toArray(new NamedVar[vals.size()]));
    }     
    
    public Element fillNamedVarElement(Document doc, NamedVar nv, String tagName) {
        Element res = doc.createElementNS("", tagName); //$NON-NLS-1$
        res.setAttribute(VNAMEDVAR_NAME_TAG, nv.getName());
        Element nvValue = doc.createElementNS("", VNAMEDVAR_VALUE_TAG); //$NON-NLS-1$
        nvValue.appendChild(fillVariantElement(doc, nv.getValue()));      
        res.appendChild(nvValue);
        return res;
    }

    public Element fillVariantElement(Document doc, Object value) {
        Element res = null;
        if(value == null)
            res = doc.createElementNS("", VARIANT_VNULL_TAG); //$NON-NLS-1$
        else if(value instanceof Integer){            
            res = doc.createElementNS("", VARIANT_VINT_TAG); //$NON-NLS-1$
            res.setAttribute(VARIANT_VALUE_ATTR_TAG, value.toString());
        }
        else if(value instanceof String){
            res = doc.createElementNS("", VARIANT_VSTRING_TAG); //$NON-NLS-1$
            res.setAttribute(VARIANT_VALUE_ATTR_TAG, (String)value);            
        }
        else if (value instanceof XMLGregorianCalendar){
            res = doc.createElementNS("", VARIANT_VDATETIME_TAG); //$NON-NLS-1$
            res.setAttribute(VARIANT_VALUE_ATTR_TAG, ((XMLGregorianCalendar)value).toXMLFormat());
        }
        else if (value instanceof Object[]){
            res = doc.createElementNS("", VARIANT_VARRAY_TAG); //$NON-NLS-1$
            Object array[] = (Object[]) value; 
            for (Object obj : array) {
                res.appendChild(doc.createElementNS("", VARRAY_VARIANT_TAG)) //$NON-NLS-1$
//                res.appendChild(doc.createElement(VARRAY_VARIANT_TAG))
                .appendChild(fillVariantElement(doc, obj));
            }
        }
        else if (value instanceof Double){
            res = doc.createElementNS("", VARIANT_VDOUBLE_TAG); //$NON-NLS-1$
            res.setAttribute(VARIANT_VALUE_ATTR_TAG, ((Double)value).toString());            
        }
        else if (value instanceof BigDecimal){
            res = doc.createElementNS("", VARIANT_VDECIMAL_TAG); //$NON-NLS-1$
            res.setAttribute(VARIANT_VALUE_ATTR_TAG, ((BigDecimal)value).toString());            
        }
        else if (value instanceof Long){
            res = doc.createElementNS("", VARIANT_VLONG_TAG); //$NON-NLS-1$
            res.setAttribute(VARIANT_VALUE_ATTR_TAG, ((Long)value).toString());            
        }          
        else if (value instanceof CLOBValue){
            CLOBValue cv = (CLOBValue) value;
            res = doc.createElementNS("", VARIANT_VCLOB_TAG); //$NON-NLS-1$
            Element mimeType = doc.createElementNS("", VCLOB_MIME_TAG); //$NON-NLS-1$
            mimeType.setTextContent(cv.getMimeType());
            Element data = doc.createElementNS("", VCLOB_DATA_TAG); //$NON-NLS-1$
            data.setTextContent(cv.getValue());
            
            res.appendChild(mimeType);
            res.appendChild(data);
            
            if(cv.getCreated()!=null){
                Element created = doc.createElementNS("", VCLOB_CREATED_TAG);             //$NON-NLS-1$
                created.setTextContent(TypeConverter.convertSqlToXml(cv.getCreated()).toString());
                res.appendChild(created);
            }
            if(cv.getCreatorNickName()!=null){
                Element nick = doc.createElementNS("", VCLOB_CREATORNICK_TAG); //$NON-NLS-1$
                nick.setTextContent(cv.getCreatorNickName());
                res.appendChild(nick);
            }
            if(cv.getCreatorId() != null){
                Element creator = doc.createElementNS("", VCLOB_CREATORID_TAG); //$NON-NLS-1$
                creator.setTextContent(cv.getCreatorId().toString());
                res.appendChild(creator);
            }
        }   
        else if(value instanceof NamedVar){
            NamedVar nv = (NamedVar)value;
            res = fillNamedVarElement(doc, nv, VARIANT_VNAMEDVAR_TAG);
        }
        else if(value instanceof VObject){
            VObject vo = (VObject)value;
            res = doc.createElementNS("", VARIANT_VOBJECT_TAG); //$NON-NLS-1$
            res.setAttribute(VOBJECT_TYPE_TAG, vo.getType());
            for (Entry<String, Object> prop : vo.getProps().entrySet()) {
                res.appendChild(fillNamedVarElement(doc, new NamedVar(prop.getKey(), prop.getValue()), VOBJECT_PROP_TAG));
            }
        }
        else if (value instanceof VUnknown){
            res = doc.createElementNS("", VARIANT_VUNKNOWN_TAG); //$NON-NLS-1$
        }          
        else
            throw new EUnknownSerializationType(value.getClass().getName());
        return res;
    }
    
    public enum SchemaId{MODIFICATION_REQUEST, SELECT_REQUEST};

    private static final String SERVER_SCHEMES[] = 
    {
        "Modification", //$NON-NLS-1$
        "Select" //$NON-NLS-1$
    };

    public InputStream getWSDLSource(){
        return null;
    }
    
    public Schema getSchema(SchemaId schId) throws SAXException, XPathExpressionException, IOException, ParserConfigurationException{

        String schName = SERVER_SCHEMES[schId.ordinal()];
        
        Node reqNode = getMessageSchemaNode(getWSDLSource(), schName, true);
/*        Node reqNode = (Node)m_xp.evaluate(
                MessageFormat.format(SCHEMA_REQUEST_PATH, schName),
                new InputSource(getWSDLSource()), 
                XPathConstants.NODE);
  */      
        SchemaFactory schFact = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        return schFact.newSchema(
                new Source[]{
                        //new DOMSource(tdsNode),
                        new DOMSource(reqNode)
                });
    }

    public Node getMessageSchemaNode(InputStream source, String opName, boolean isRequest) throws XPathExpressionException, ParserConfigurationException {
        
        XPathFactory xpFact = XPathFactory.newInstance();
        XPath m_xp = xpFact.newXPath();
        m_xp.setNamespaceContext(this);

        
        DocumentBuilderFactory docFact = DocumentBuilderFactory.newInstance();
        docFact.setNamespaceAware(true);
        Document doc=null;
        doc = docFact.newDocumentBuilder().newDocument();
    
        Element sch = (Element) doc.appendChild(doc.createElementNS(XSD_NAMESPACE, "schema")); //$NON-NLS-1$
        sch.setAttribute("targetNamespace", TF_SOAP_NAMESPACE); //$NON-NLS-1$
        sch.setAttribute("xmlns:tf", TF_TDS_NAMESPACE); //$NON-NLS-1$
        sch.setAttribute("xmlns:xsd", XSD_NAMESPACE); //$NON-NLS-1$
//        Element e = (Element)sch.appendChild(doc.createElementNS(XSD_NAMESPACE, "import"));
//        e.setAttribute("namespace", TF_TDS_NAMESPACE);
        Element e = (Element)sch.appendChild(doc.createElementNS(XSD_NAMESPACE, "element")); //$NON-NLS-1$
        
        String msgName;
        String msgTag = isRequest? "input" : "output"; //$NON-NLS-1$ //$NON-NLS-2$
        
        Element e2 = (Element)m_xp.evaluate(
                MessageFormat.format("/wsdl:definitions/wsdl:portType/wsdl:operation[@name=''{0}'']/wsdl:{1}", opName, msgTag), //$NON-NLS-1$
                new InputSource(getWSDLSource()), 
                XPathConstants.NODE);
        msgName = e2.getAttribute("message"); //$NON-NLS-1$
        msgName = msgName.substring(msgName.indexOf(":")+1); //$NON-NLS-1$
        e2 = (Element)m_xp.evaluate(
                MessageFormat.format("/wsdl:definitions/wsdl:message[@name=''{0}'']/wsdl:part", msgName), //$NON-NLS-1$
                new InputSource(getWSDLSource()), 
                XPathConstants.NODE);
        String msgPartName = e2.getAttribute("name"); //$NON-NLS-1$
        String msgPartType = e2.getAttribute("type");  //$NON-NLS-1$
        msgPartType = msgPartType.substring(msgPartType.indexOf(":")+1); //$NON-NLS-1$
        
        if(!isRequest)
            opName = msgName;
        
        e.setAttribute("name", opName); //$NON-NLS-1$
        e = (Element)e.appendChild(doc.createElementNS(XSD_NAMESPACE, "complexType")); //$NON-NLS-1$
        e = (Element)e.appendChild(doc.createElementNS(XSD_NAMESPACE, "sequence")); //$NON-NLS-1$
        e = (Element)e.appendChild(doc.createElementNS(XSD_NAMESPACE, "element")); //$NON-NLS-1$
        e.setAttribute("name", msgPartName); //$NON-NLS-1$
        
        e.setAttribute("type", "tf:"+msgPartType); //$NON-NLS-1$ //$NON-NLS-2$
        
        //import all types because TDS_NS = SOAP_NS
        NodeList tfTypes = (NodeList) m_xp.evaluate(
                "/wsdl:definitions/wsdl:types/xsd:schema/child::*", //$NON-NLS-1$
                new InputSource(getWSDLSource()), 
                XPathConstants.NODESET);

        for(int i=0; i!=tfTypes.getLength(); i++){
            e2 = (Element) tfTypes.item(i);
            e2 = (Element) doc.importNode(e2, true);
            sch.appendChild(e2);
        }
        
        return doc;
    }

    @Override
	public String getNamespaceURI(String pref) {
        
        String prefs[] = {"soap", "tf", "wsdl", "xsd", "xsd1"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        String namespaces[] = {
                "http://schemas.xmlsoap.org/wsdl/soap/",  //$NON-NLS-1$
                TF_SOAP_NAMESPACE, 
                "http://schemas.xmlsoap.org/wsdl/",  //$NON-NLS-1$
                XSD_NAMESPACE, 
                TF_TDS_NAMESPACE};
        
        int idx = Arrays.binarySearch(prefs, pref);
        return idx < 0 ? null : namespaces[idx];
    }

    @Override
	public String getPrefix(String arg0) {
        return null;
    }

    @Override
	public Iterator getPrefixes(String arg0) {
        return null;
    }
    
    /*public Element fillExceptionElement(Document doc, Exception e) {
        
        String tagName = e instanceof EAuth ? EAUTH_TAG : EGENERAL_ERROR_TAG;
        
        Element eNode = doc.createElementNS(TF_SOAP_NAMESPACE, tagName);
        eNode.appendChild(doc.createElement(EGENERROR_MESSAGE_TAG)).setTextContent(e.getMessage());
        eNode.appendChild(doc.createElement(EGENERROR_EXCNAME_TAG)).setTextContent(e.getClass().getName());
        eNode.appendChild(doc.createElement(EGENERROR_LOCATION_TAG)).setTextContent("location"); //$NON-NLS-1$
        StringBuffer buf = new StringBuffer();
        for (StackTraceElement traceElement : e.getStackTrace()) {
            buf.append(MessageFormat.format("at {0}.{1}({2}:{3})\n", traceElement.getClassName(), traceElement.getMethodName(), traceElement.getFileName(), traceElement.getLineNumber())); //$NON-NLS-1$
        }
        eNode.appendChild(doc.createElement(EGENERROR_STACK_TAG)).setTextContent(buf.toString());
     
        if(e.getClass() != Exception.class){
            Class parentClass = e.getClass(); 
            while((parentClass = parentClass.getSuperclass()) != Exception.class){
                eNode.appendChild(doc.createElement(EGENERROR_ANCESTOR_TAG)).setTextContent(parentClass.getName());                
            }
        }
        
        ((Element)eNode.appendChild(doc.createElementNS(AXIS_NAMESPACE, AXIS_EXCEPTION_NAME_TAG))).setTextContent(e.getClass().getName());
        ((Element)eNode.appendChild(doc.createElementNS(AXIS_NAMESPACE, AXIS_HOSTNAME_TAG))).setTextContent(DEFAULT_HOSTNAME);
        return eNode;
    }*/

    public NamedVar createNamedVar(Element node) throws DOMException, ParseException, XPathExpressionException {
        return new NamedVar(
                node.getAttribute(VNAMEDVAR_NAME_TAG),
                createVariantObject((Element) ((Element)node.getElementsByTagName(VNAMEDVAR_VALUE_TAG).item(0)).getElementsByTagName("*").item(0))                 //$NON-NLS-1$
        );
    }

}
