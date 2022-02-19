package de.regatta_hd.aquarius.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
* The entity contains a log record.
*/
@Entity
@Table(schema = "dbo", name = "LogRecord")
//lombok
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class LogRecord {

	public LogRecord(String hostName, String hostAddr, java.util.logging.LogRecord logRecord) {
		setInstant(logRecord.getInstant());
		setLogger(logRecord.getLoggerName());
		setMessage(logRecord.getMessage());
		setThreadID(logRecord.getThreadID());
		setHostName(hostName);
		setHostIP(hostAddr);
		setLevelName(logRecord.getLevel().getName());
		setLevelValue(logRecord.getLevel().intValue());
		setSourceClass(logRecord.getSourceClassName());
		setSourceMethod(logRecord.getSourceMethodName());

		if (logRecord.getThrown() != null) {
			setThrowableClass(logRecord.getThrown().getClass().getName());
			try (StringWriter strWriter = new StringWriter(); PrintWriter writer = new PrintWriter(strWriter);) {
				logRecord.getThrown().printStackTrace(writer);
				setStackTrace(strWriter.toString());
			} catch (IOException e) {
				// ignored
			}
		}
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	/**
	 * Event time.
	 */
	@Column(name = "instant")
	private Instant instant;

	@Column(name = "hostName")
	private String hostName;

	@Column(name = "hostIP")
	private String hostIP;

	@Column(name = "levelName")
	private String levelName;

	@Column(name = "levelValue")
	private int levelValue;

	/**
	 * Name of the source Logger.
	 */
	@Column(name = "logger")
	private String logger;

	/**
	 * Class that issued logging call
	 */
	@Column(name = "sourceClass")
	private String sourceClass;

	/**
	 * Method that issued logging call
	 */
	@Column(name = "sourceMethod")
	private String sourceMethod;

	/**
	 * Non-localized raw message text
	 */
	@Column(name = "message")
	private String message;

	/**
	 * Thread ID for thread that issued logging call.
	 */
	@Column(name = "threadId")
	private int threadID;

	@Column(name = "stackTrace")
	private String stackTrace;

	@Column(name = "throwableClass")
	private String throwableClass;
}
