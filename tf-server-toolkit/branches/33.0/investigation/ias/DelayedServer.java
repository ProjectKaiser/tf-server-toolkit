package ias;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import junit.framework.TestCase;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.triniforce.jsonrpc.JSONRPCConnection;
import com.triniforce.utils.ApiAlgs;

public class DelayedServer extends TestCase {

	final static String jsonRequest = "{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": [42, 23], \"id\": 1}";
	final static String jsonResponse = "{\"jsonrpc\": \"2.0\", \"result\": 19, \"id\": 1}";
	final static String jsonRequest2 = "{\"jsonrpc\": \"2.0\", \"method\": \"add\", \"params\": [22, 33], \"id\": 1}";
	final static String jsonResponse2 = "{\"jsonrpc\": \"2.0\", \"result\": 55, \"id\": 1}";

	public void testInvoke() throws IOException  {
		final int port = getFreePort();
		HttpServer server = createTestHttpServer(port, "/test", jsonRequest, jsonResponse, false);
		try {
			JSONRPCConnection jsonConn = new JSONRPCConnection("http://localhost:" + port + "/test", 100, 100);
			assertEquals(jsonResponse, jsonConn.invoke(jsonRequest));
		} finally {
			server.stop(0);
		}
	}

	public void testInvokeTimeout() throws IOException  {
		final int port = getFreePort();
		HttpServer server = createTestHttpServer(port, "/test", jsonRequest, jsonResponse, true);
		try {
			try {
				JSONRPCConnection jsonConn = new JSONRPCConnection("http://localhost:" + port + "/test", 100, 100);
				assertEquals(jsonResponse, jsonConn.invoke(jsonRequest));
				fail("Expected exception");
			} catch (ApiAlgs.RethrownException e) {
				assertEquals(SocketTimeoutException.class, e.getCause().getClass());
				assertEquals("Read timed out", e.getCause().getMessage());
			}
		} finally {
			server.stop(0);
		}
	}

	private int getFreePort() throws IOException {
		ServerSocket server = new ServerSocket(0);
		int port = server.getLocalPort();
		server.close();
		return port;
	}

	private HttpServer createTestHttpServer(int port, String context, final String request,
			final String response, final boolean delayResponse) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext(context, new HttpHandler() {
			public void handle(HttpExchange paramHttpExchange) throws IOException {
				// check request
				InputStream is = paramHttpExchange.getRequestBody();
				try {
					ApiAlgs.getLog(this).trace("REQUEST: " + new String(JSONRPCConnection.readAllBytesFromInputStream(
							is, -1), "UTF-8"));
				} finally {
					try { is.close(); } catch (Throwable e) { }
				}
				try {
					Thread.sleep(12000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				// send response
				paramHttpExchange.sendResponseHeaders(200, response.length());
				if (delayResponse)
					try { Thread.sleep(200); } catch (InterruptedException e) { }
				OutputStream os = paramHttpExchange.getResponseBody();
				try {
					os.write(response.getBytes());
					//os.flush();
				} finally {
					try { os.close(); } catch (Throwable e) { }
				}
			}
		});
		server.setExecutor(null); // creates a default executor
		server.start();
		return server;
	}

	public void testKeepAlive() throws IOException {
		ServerSocket server = new SimpleOneConnectionJsonHttpSocketServer();
		try {
			int port = server.getLocalPort();
			JSONRPCConnection jsonConn = new JSONRPCConnection("http://localhost:" + port + "/test", 100, 100);
			assertEquals(jsonResponse, jsonConn.invoke(jsonRequest));
			assertEquals(jsonResponse2, jsonConn.invoke(jsonRequest2));
		} finally{
			server.close();
		}
	}
	
	class SimpleOneConnectionJsonHttpSocketServer extends ServerSocket implements Runnable {
		public SimpleOneConnectionJsonHttpSocketServer() throws IOException {
			super(0);
			new Thread(this).start();
		}
		static final byte CR = 13, LF = 10;
		private int skipRequestHeaderAndGetContentLength(InputStream is) throws IOException {
			int c, contentLength = -1;
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			while((c = is.read()) >= 0) {
				if (c == LF) {
					String str = new String(buffer.toByteArray());
					buffer.reset();
					if (str.isEmpty()) break;
					if (str.toLowerCase().startsWith("content-length:"))
						contentLength = Integer.parseInt(str.substring("content-length:".length()).trim());
				} else if ( c != CR) {
					buffer.write(c);
				}
			}
			return contentLength;
		}
		public void run() {
			try {
				Socket socket = accept();
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
				int requestLength = skipRequestHeaderAndGetContentLength(is);
				assertEquals(jsonRequest.length(), requestLength);
				assertEquals(jsonRequest, new String(JSONRPCConnection.readAllBytesFromInputStream(
						is, requestLength), "UTF-8"));
				os.write(("HTTP/1.1 200 OK\r\n" + "Content-Length: " + jsonResponse.length()
						+ "\r\n\r\n" + jsonResponse).getBytes());
				requestLength = skipRequestHeaderAndGetContentLength(is);
				assertEquals(jsonRequest2.length(), requestLength);
				assertEquals(jsonRequest2, new String(JSONRPCConnection.readAllBytesFromInputStream(
						is, requestLength), "UTF-8"));
				os.write(("HTTP/1.1 200 OK\r\n" + "Content-Length: " + jsonResponse2.length()
						+ "\r\n\r\n" + jsonResponse2).getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		DelayedServer s = new DelayedServer();
		
		try {
			HttpServer server = s.createTestHttpServer(3064, "/UBL/jsapi", jsonRequest, jsonResponse, true);
			int c;
			do{
				c = System.in.read();
			}while(c!='q');
			server.stop(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
