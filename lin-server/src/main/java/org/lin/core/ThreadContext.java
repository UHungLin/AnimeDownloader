package org.lin.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * @author Lin =￣ω￣=
 * @date 2021/6/28
 */
public class ThreadContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadContext.class);

	public static final String LIN_GET_THREAD_DOWNLOADER = "Lin-Get-Downloader";

	public static final String LIN_GET_THREAD = "Lin-Get-Timer";


	private static final ScheduledExecutorService EXECUTOR_TIMER;

	static {
		EXECUTOR_TIMER = ThreadContext.newTimerExecutor(1, LIN_GET_THREAD);
	}

	public static final ExecutorService newExecutor(int corePoolSize, int maximumPoolSize,
	                                                int queueSize, long keepAliveTime, String name) {
		return new ThreadPoolExecutor(
			corePoolSize,
			maximumPoolSize,
			keepAliveTime,
			TimeUnit.SECONDS,
			new LinkedBlockingDeque<Runnable>(queueSize),
			newThreadFactory(name),
			new ThreadPoolExecutor.DiscardPolicy()
		);
	}

	public static final ScheduledExecutorService newTimerExecutor(int corePoolSize, String name) {
		return new ScheduledThreadPoolExecutor(
			corePoolSize,
			newThreadFactory(name)
		);
	}

	public static final ScheduledFuture<?> timer(long delay, long period, TimeUnit unit, Runnable runnable) {
		return EXECUTOR_TIMER.scheduleAtFixedRate(runnable, delay, period, unit);
	}

	public static final void shutdown() {
		shutdown(EXECUTOR_TIMER);
	}

	public static final void shutdown(ExecutorService executor) {
		if(executor == null || executor.isShutdown()) {
			return;
		}
		try {
			executor.shutdown();
		} catch (Exception e) {
			LOGGER.error("close executor error", e);
		}
	}

	public static final void shutdown(ScheduledFuture<?> scheduledFuture) {
		if (scheduledFuture == null || scheduledFuture.isCancelled()) {
			return;
		}
		try {
			scheduledFuture.cancel(true);
		} catch (Exception e) {
			LOGGER.error("close scheduledFuture error", e);
		}
	}

	private static final ThreadFactory newThreadFactory(String poolName) {
		return new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				final Thread thread = new Thread(runnable);
				thread.setName(poolName);
				thread.setDaemon(true);
				return thread;
			}
		};
	}

}
