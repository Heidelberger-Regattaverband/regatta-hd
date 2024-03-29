package de.regatta_hd.commons.db;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBThreadPoolExecutor extends ThreadPoolExecutor {
	private final Logger logger = Logger.getLogger(DBThreadPoolExecutor.class.getName());

	static final String DB_THREAD_PREFIX = "Database-Connection-";

	public DBThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(),
				new DBThreadPoolFactory());
	}

	@Override
	protected void beforeExecute(Thread thread, Runnable r) {
		this.logger.log(Level.FINEST, "Before executing runnable {0} in thread {1}.",
				new Object[] { r.getClass(), thread.getName() });
		super.beforeExecute(thread, r);
	}

	@Override
	protected void afterExecute(Runnable r, Throwable thread) {
		super.afterExecute(r, thread);
		this.logger.log(Level.FINEST, "After executing runnable {0}.", r.getClass());
	}

	@Override
	protected void terminated() {
		super.terminated();
		this.logger.log(Level.FINEST, "Terminated DBThreadPoolExecutor.");
	}

	private static class DBThreadPoolFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);

		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable, DB_THREAD_PREFIX + poolNumber.getAndIncrement() + "-thread");
			thread.setDaemon(true);
			return thread;
		}
	}
}