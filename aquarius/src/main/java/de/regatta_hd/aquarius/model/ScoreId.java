package de.regatta_hd.aquarius.model;

import java.io.Serializable;

import lombok.Data;

/**
 * The ID class for entity {@link Score}.
 */
@Data
public class ScoreId implements Serializable {

	private static final long serialVersionUID = 3082146068781602948L;

	static {
		// register persistent class in JVM
		try {
			Class.forName("de.regatta_hd.aquarius.model.Score");
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	private int clubId;

	private int regattaId;

}