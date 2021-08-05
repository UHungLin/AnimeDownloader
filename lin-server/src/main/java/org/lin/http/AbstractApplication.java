package org.lin.http;

import org.lin.controller.AnimeController;
import org.lin.util.OSUtil;
import org.lin.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lin =￣ω￣=
 * @date 2021/8/2
 */
public abstract class AbstractApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractApplication.class);

	private static final int REST_PORT = 23333;

	private static final int WEBSOCKET_PORT = 9090;

	private HttpServer httpServer;

	private WebSocketServer webSocketServer;

	public void setup() throws Exception {
		doCheck();
		setupServer();
	}

	protected abstract void setupServer() throws Exception;

	public void startServer() throws Exception {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				stopServer();
			} catch (Exception e) {
			}
		}));
		if (httpServer != null) {
			LOGGER.info("http server starting up...");
			httpServer.start();
//			httpServer.join();
		}
		if (webSocketServer != null) {
			LOGGER.info("websocket server starting up...");
			webSocketServer.start();
//			webSocketServer.join();
		}
	}

	public void stopServer() throws Exception {
		if (httpServer != null) {
			httpServer.stopServer();
		}
		if (webSocketServer != null) {
			webSocketServer.closeServer();
		}
	}

	protected void setupHttpServer() {
		httpServer = new HttpServer(REST_PORT);
		httpServer.addController(new AnimeController());
	}

	protected void setupWebSocketServer() {
		webSocketServer = new WebSocketServer("127.0.0.1", WEBSOCKET_PORT);
	}

	private static void doCheck() {
		if (OSUtil.isBusyPort(REST_PORT) || OSUtil.isBusyPort(WEBSOCKET_PORT)) {
			LOGGER.error("port is occupied");
			System.exit(0);
		}
	}

}
