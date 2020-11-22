package de.regatta_hd.aquarius.db.model;

import java.io.Serializable;

/**
 * Application identity class for: de.regatta_hd.aquarius.db.model.ProgressionRule
 *
 * Auto-generated by: org.apache.openjpa.enhance.ApplicationIdTool
 */
public class ProgressionRuleId implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 7329815979312139901L;

	static {
		// register persistent class in JVM
		try {
			Class.forName("de.regatta_hd.aquarius.db.model.ProgressionRule");
		} catch (Exception e) {
		}
	}

	public int prId;

	public ProgressionRuleId() {
	}

	public ProgressionRuleId(String str) {
		fromString(str);
	}

	public int getPrId() {
		return this.prId;
	}

	public void setPrId(int prId) {
		this.prId = prId;
	}

	@Override
	public String toString() {
		return String.valueOf(this.prId);
	}

	@Override
	public int hashCode() {
		return this.prId;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || obj.getClass() != getClass())
			return false;

		ProgressionRuleId other = (ProgressionRuleId) obj;
		return (this.prId == other.prId);
	}

	private void fromString(String str) {
		this.prId = Integer.parseInt(str);
	}
}