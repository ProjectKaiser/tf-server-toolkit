package com.triniforce.jsonrpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

import junit.framework.TestCase;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.triniforce.utils.ApiAlgs;

public class JSONRPCConnectionTest extends TestCase {

	final String jsonRequest = "{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": [42, 23], \"id\": 1}";
	final String jsonResponse = "{\"jsonrpc\": \"2.0\", \"result\": 19, \"id\": 1}";

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
					assertEquals(request, new String(JSONRPCConnection.readAllBytesFromInputStream(
							is, -1), "UTF-8"));
				} finally {
					try { is.close(); } catch (Throwable e) { }
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

}
