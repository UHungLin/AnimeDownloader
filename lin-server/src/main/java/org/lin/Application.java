package org.lin;

import org.lin.core.BilibiliClientCore;
import org.lin.core.DelayHandleCore;
import org.lin.core.MessageCore;
import org.lin.core.ThreadContext;
import org.lin.downloader.DownloaderManager;
import org.lin.http.AbstractApplication;
import org.lin.parser.ParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lin =￣ω￣=
 * @date 2021/7/13
 */
public class Application extends AbstractApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	private static Application application;

	public static void main(String[] args) {
		application = new Application();
		try {
			application.setup();
			application.startServer();
		} catch (Throwable e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

	}

	@Override
	protected void setupServer() throws Exception {
		setupHttpServer();

		setupWebSocketServer();

		ParserManager.getInstance().init();

		DownloaderManager.getInstance().init();

		BilibiliClientCore.init();

		MessageCore.getInstance().init();

		DelayHandleCore.getInstance().init();
	}

	@Override
	public void stopServer() throws Exception {
		super.stopServer();

		DownloaderManager.getInstance().stop();

		MessageCore.getInstance().stop();

		DelayHandleCore.getInstance().stop();

		ThreadContext.shutdown();
	}

	public static Application application() {
		return application;
	}

}

