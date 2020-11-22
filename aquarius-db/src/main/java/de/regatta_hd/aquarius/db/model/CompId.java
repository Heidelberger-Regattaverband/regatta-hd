package de.regatta_hd.aquarius.db.model;

import java.io.Serializable;

/**
 * Application identity class for: de.regatta_hd.aquarius.db.model.Comp
 *
 * Auto-generated by: org.apache.openjpa.enhance.ApplicationIdTool
 */
public class CompId implements Serializable {

	private static final long serialVersionUID = 5211664889749190960L;

	static {
		// register persistent class in JVM
		try {
			Class.forName("de.regatta_hd.aquarius.db.model.Comp");
		} catch (Exception e) {
		}
	}

	public int compID;

	public CompId() {
	}

	public CompId(String str) {
		fromString(str);
	}

	public int getCompID() {
		return this.compID;
	}

	public void setCompID(int compID) {
		this.compID = compID;
	}

	@Override
	public String toString() {
		return String.valueOf(this.compID);
	}

	@Override
	public int hashCode() {
		return this.compID;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || obj.getClass() != getClass())
			return false;

		CompId other = (CompId) obj;
		return (this.compID == other.compID);
	}

	private void fromString(String str) {
		this.compID = Integer.parseInt(str);
	}
}