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
		setLoggerName(logRecord.getLoggerName());
		setMessage(logRecord.getMessage());
		setThreadID(logRecord.getThreadID());
		setHostName(hostName);
		setHostAddress(hostAddr);
		setLevelName(logRecord.getLevel().getName());
		setLevelValue(logRecord.getLevel().intValue());
		setSourceClassName(logRecord.getSourceClassName());
		setSourceMethodName(logRecord.getSourceMethodName());

		if (logRecord.getThrown() != null) {
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

	@Column(name = "hostAddress")
	private String hostAddress;

	@Column(name = "levelName")
	private String levelName;

	@Column(name = "levelValue")
	private int levelValue;

	/**
	 * Name of the source Logger.
	 */
	@Column(name = "loggerName")
	private String loggerName;

	/**
	 * Class that issued logging call
	 */
	@Column(name = "sourceClassName")
	private String sourceClassName;

	/**
	 * Method that issued logging call
	 */
	@Column(name = "sourceMethodName")
	private String sourceMethodName;

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

	@Column(name = "stacktrace")
	private String stackTrace;
}
