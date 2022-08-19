package de.regatta_hd.aquarius.model;

import java.io.Serializable;

import lombok.Data;

/**
 * The ID class for entity {@link Result}.
 */
@Data
public class ResultId implements Serializable {

	private static final long serialVersionUID = -8395173645471651953L;

	static {
		// register persistent class in JVM
		try {
			Class.forName("de.regatta_hd.aquarius.model.Result");
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	private int heatRegistrationId;

	private byte splitNr;
}