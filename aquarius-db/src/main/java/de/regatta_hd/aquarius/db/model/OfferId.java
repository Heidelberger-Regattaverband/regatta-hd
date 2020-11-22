package de.regatta_hd.aquarius.db.model;

import java.io.Serializable;

/**
 * Application identity class for: de.regatta_hd.aquarius.db.model.Offer
 *
 * Auto-generated by: org.apache.openjpa.enhance.ApplicationIdTool
 */
public class OfferId implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 4115901786290024099L;

	static {
		// register persistent class in JVM
		try {
			Class.forName("de.regatta_hd.aquarius.db.model.Offer");
		} catch (Exception e) {
		}
	}

	public int offerID;

	public OfferId() {
	}

	public OfferId(String str) {
		fromString(str);
	}

	public int getOfferID() {
		return this.offerID;
	}

	public void setOfferID(int offerID) {
		this.offerID = offerID;
	}

	@Override
	public String toString() {
		return String.valueOf(this.offerID);
	}

	@Override
	public int hashCode() {
		return this.offerID;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || obj.getClass() != getClass())
			return false;

		OfferId other = (OfferId) obj;
		return (this.offerID == other.offerID);
	}

	private void fromString(String str) {
		this.offerID = Integer.parseInt(str);
	}
}