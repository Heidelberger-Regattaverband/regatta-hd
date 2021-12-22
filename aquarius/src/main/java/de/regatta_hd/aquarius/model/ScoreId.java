package de.regatta_hd.aquarius.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The ID class for entity {@link Score}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreId implements Serializable {
	private static final long serialVersionUID = 1140277898086880875L;

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