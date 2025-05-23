package de.regatta_hd.aquarius.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
* The entity contains a log record.
*/
@Entity
@Table(schema = "dbo", name = "HRV_LogRecord")
//lombok
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LogRecord implements Serializable {
	private static final long serialVersionUID = 1984221053172318902L;

	private static final Logger log = Logger.getLogger(LogRecord.class.getName());

	/**
	 * Event time.
	 */
	@Id
	@Column(name = "instant")
	@EqualsAndHashCode.Include
	private Instant instant;

	@Column(name = "hostName")
	private String hostName;

	@Column(name = "hostAddress")
	private String hostAddress;

	/**
	 * The name of the log level, e.g. INFO or WARN.
	 */
	@Column(name = "levelName")
	private String levelName;

	/**
	 * An integer value representing the log level.
	 */
	@Column(name = "levelValue")
	private int levelValue;

	/**
	 * Name of the source Logger.
	 */
	@Column(name = "logger")
	private String logger;

	/**
	 * Class that issued logging call.
	 */
	@Column(name = "sourceClass")
	private String sourceClass;

	/**
	 * Method that issued logging call.
	 */
	@Column(name = "sourceMethod")
	private String sourceMethod;

	/**
	 * Non-localized raw message text
	 */
	@Id
	@Column(name = "message")
	private String message;

	/**
	 * ID of the thread that issued logging call.
	 */
	@Column(name = "threadId")
	private long threadID;

	/**
	 * Name of the thread that issued logging call.
	 */
	@Column(name = "threadName")
	private String threadName;

	@Column(name = "stackTrace")
	private String stackTrace;

	@Column(name = "throwable")
	private String throwable;

	public LogRecord(String hostName, String hostAddr, java.util.logging.LogRecord logRecord) {
		setInstant(logRecord.getInstant());
		setLogger(logRecord.getLoggerName());
		setMessage(MessageFormat.format(logRecord.getMessage(), logRecord.getParameters()));
		setThreadID(logRecord.getLongThreadID());
		setHostName(hostName);
		setHostAddress(hostAddr);
		setSourceClass(logRecord.getSourceClassName());
		setSourceMethod(logRecord.getSourceMethodName());

		Optional<Thread> threadOpt = Thread.getAllStackTraces().keySet().stream()
				.filter(thread -> thread.threadId() == logRecord.getLongThreadID()).findFirst();
		if (threadOpt.isPresent()) {
			setThreadName(threadOpt.get().getName());
		}

		if (logRecord.getLevel() != null) {
			setLevelName(logRecord.getLevel().getName());
			setLevelValue(logRecord.getLevel().intValue());
		}

		if (logRecord.getThrown() != null) {
			setThrowable(logRecord.getThrown().getClass().getName());
			try (StringWriter strWriter = new StringWriter(); PrintWriter writer = new PrintWriter(strWriter);) {
				logRecord.getThrown().printStackTrace(writer);
				setStackTrace(strWriter.toString());
			} catch (IOException e) {
				log.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

}
