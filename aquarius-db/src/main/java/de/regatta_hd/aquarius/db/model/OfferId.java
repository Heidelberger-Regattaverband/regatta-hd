package de.regatta_hd.aquarius.db.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Application identity class for: de.regatta_hd.aquarius.db.model.Offer
 *
 * Auto-generated by: org.apache.openjpa.enhance.ApplicationIdTool
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfferId implements Serializable {

	private static final long serialVersionUID = 4115901786290024099L;

	static {
		// register persistent class in JVM
		try {
			Class.forName("de.regatta_hd.aquarius.db.model.Offer");
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	private int id;
}