package de.regatta_hd.aquarius.db.model;

import java.io.Serializable;

import lombok.Data;

/**
 * Application identity class for: de.regatta_hd.aquarius.db.model.EntryLabel
 *
 * Auto-generated by: org.apache.openjpa.enhance.ApplicationIdTool
 */
@Data
public class EntryLabelId implements Serializable {

	private static final long serialVersionUID = -5970601311571149673L;

	static {
		// register persistent class in JVM
		try {
			Class.forName("de.regatta_hd.aquarius.db.model.EntryLabel");
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	private int id;
}