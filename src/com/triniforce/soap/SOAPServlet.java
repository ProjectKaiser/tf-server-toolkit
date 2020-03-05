package com.triniforce.soap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import com.triniforce.soap.RequestHandler.IServiceInvoker;
import com.triniforce.utils.ApiAlgs;

/**
 * Servlet implementation class for Servlet: PK
 *
 */
 public abstract class SOAPServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	private static final long serialVersionUID = -3007557683958849944L;
	

	protected InterfaceDescriptionGenerator m_gen;
	protected InterfaceDescription m_desc;
	protected Object m_service;

	private String m_ns;
	private String m_name;

	private Package m_pkg;

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public SOAPServlet(String ns, String name, Package pkg) {
		this(ns, name, pkg, null);
	}   
	
	public SOAPServlet(String ns, String name, Package pkg, String interfaceFName){
		super();
		
		m_ns = ns;
		m_name = name;
		m_pkg = pkg;
		m_gen = new InterfaceDescriptionGenerator(m_ns, m_name);
	}
	
	@Override
	public void init(ServletConfig arg0) throws ServletException {
		super.init(arg0);
		try {
			InterfaceDescription oldDesc = null;
//			InputStream itfRs = getInterfaceDescResource();
//			if(null != itfRs)
//			try{
//				oldDesc = loadInterfaceDescription(itfRs);
//			}finally{
//				itfRs.close();
//			}
			m_service = createService();
			m_desc = generateInterfaceDescription(oldDesc);
		} catch (Exception e) {
			ApiAlgs.rethrowException(e);
		}
		
	}


	protected List<InterfaceOperationDescription> getIOperationDescs() {
		return null;
	}
	
	public abstract Object createService();
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doRequest(request, response);
	}  	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doRequest(request, response);
	}   
	
	protected void doRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
				
		if("wsdl".equals(request.getQueryString())){
			ApiAlgs.getLog(this).trace("GEN WSDL");
			Document wsdl = m_gen.generateWSDL(m_desc.getWsdlDescription(), request.getRequestURL().toString());
			try {
				OutputStream out = response.getOutputStream();
				response.setContentType("text/xml; charset=utf-8");
				m_gen.writeDocument(out, wsdl);
			} catch (TransformerException e) {
				e.printStackTrace();
			}
		}
		else{
			doServiceCall(request, response);
		}
	}
	
	enum ProtocolType{JSON, SOAP};

	protected void doServiceCall(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    //LATER gmp: limit total size of buffer?
		ServletInputStream is = request.getInputStream(); 
		byte[] buf = new byte[10000];
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		int nr;
		while((nr = is.read(buf)) > 0){
			byteOut.write(buf, 0, nr);
		}
		buf = byteOut.toByteArray();
		nr = buf.length;
		if(nr >=0){
			ByteArrayInputStream in = new ByteArrayInputStream(buf, 0, nr);
			IServiceInvoker serviceInvoker = m_service instanceof IServiceInvoker ? (IServiceInvoker) m_service 
					: new RequestHandler.ReflectServiceInvoker(m_service);
			RequestHandler reqHandler = new RequestHandler(m_gen, m_desc, serviceInvoker);
			ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
			
			int status = HttpServletResponse.SC_OK;
			ProtocolType protocol = getProtocolType(request.getContentType().toLowerCase(Locale.ENGLISH));
			
			if(ProtocolType.JSON.equals(protocol)){
	            response.setContentType("text/json; charset=utf-8");
	            status = reqHandler.execJson(in, outBuf);
			}
			else{
	            response.setContentType("text/xml; charset=utf-8");
	            reqHandler.exec(in, outBuf);
			}
			
			byte[] ba = outBuf.toByteArray();
			response.setContentLength(ba.length);
			ServletOutputStream out = response.getOutputStream(); 
			out.write(ba, 0, ba.length);
			outBuf.close();
			response.setStatus(status);
			response.flushBuffer();
		}
	}
	
	static Pattern PROTOCOL_PTTRN = Pattern.compile("(application|text)\\/(json|xml|soap\\+xml).*");
	public static class EUnsupportedContent extends RuntimeException{
		public EUnsupportedContent(String str) {
			super(str);
		}

		private static final long serialVersionUID = 1L;
	}
	
	private ProtocolType getProtocolType(String str) {
		Matcher m = PROTOCOL_PTTRN.matcher(str);
		if(m.matches()){
			ProtocolType res;
			if(m.group(2).equals("json")){
				res = ProtocolType.JSON;
			}
			else 
				res = ProtocolType.SOAP;		
			return res;
		}
		else{
			throw new EUnsupportedContent(str);
		}
	}

	protected RequestHandler getHandler(){
		IServiceInvoker serviceInvoker = m_service instanceof IServiceInvoker ? (IServiceInvoker) m_service 
				: new RequestHandler.ReflectServiceInvoker(m_service);
		RequestHandler reqHandler = new RequestHandler(m_gen, m_desc, serviceInvoker);
		return reqHandler;
	}
	
	/**
	 * Create InterfaceDescription resource file. For build installation use.
	 * Function works with file: resourceFolder/classLocation/className.itfdesc
	 * It loads old InterfaceDescription, updates file and rewrites resource file 
	 * @param resourceFolder - Resource folder. 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
//	public void updateServiceDescription(File resourceFolder) throws IOException, ClassNotFoundException{
//		String resource = getClass().getName().replace(".", System.getProperty("file.separator"))+".itfdesc";
//		resourceFolder = new File(resourceFolder, resource);
//		InterfaceDescription oldDesc = null;
//		if(resourceFolder.exists()){
//			FileInputStream fin = new FileInputStream(resourceFolder);
//			try{
//				oldDesc = loadInterfaceDescription(fin);
//			} finally{
//				fin.close();
//			}
//		}
//		
//		InterfaceDescription desc = generateInterfaceDescription(oldDesc);
//		
//		FileOutputStream fout = new FileOutputStream(resourceFolder);
//		ObjectOutputStream oout = new ObjectOutputStream(fout);
//		try{
//			oout.writeObject(desc);
//			oout.flush();
//		}finally{
//			oout.close();
//			fout.close();
//		}
//	}

	protected abstract InterfaceDescription generateInterfaceDescription(
			InterfaceDescription oldDesc);
	
//	private InterfaceDescription loadInterfaceDescription(InputStream fin) throws IOException, ClassNotFoundException {
//		ObjectInputStream oin = new ObjectInputStream(fin);
//		InterfaceDescription res = (InterfaceDescription) oin.readObject();
//		oin.close();
//		return res;
//	}
	
//	private InputStream getInterfaceDescResource() throws FileNotFoundException {
//		String clsName = getClass().getName();
//		String descRsName = clsName.substring(clsName.lastIndexOf(".")+1) + ".itfdesc";
//		InputStream res = getClass().getResourceAsStream(descRsName);
//		return res;
//
//	}
	
	public Package getInterfacePackage(){
		return m_pkg;
	}

	public InterfaceDescriptionGenerator getInterfaceGenerator() {
		return m_gen;
	}

	public InterfaceDescription getInterfaceDescription() {
		return m_desc;
	}

	public Object getService() {
		return m_service;
	}
}