package de.regatta_hd.aquarius.db;

import java.io.*;
import java.util.*;

/**
 * Application identity class for: de.regatta_hd.aquarius.db.Referee
 *
 * Auto-generated by:
 * org.apache.openjpa.enhance.ApplicationIdTool
 */
public class RefereeId implements Serializable {
	static {
		// register persistent class in JVM
		try { Class.forName("de.regatta_hd.aquarius.db.Referee"); }
		catch(Exception e) {}
	}

	public int refereeID;

	public RefereeId() {
	}

	public RefereeId(String str) {
		fromString(str);
	}

	public int getRefereeID() {
		return refereeID;
	}

	public void setRefereeID(int refereeID) {
		this.refereeID = refereeID;
	}

	public String toString() {
		return String.valueOf(refereeID);
	}

	public int hashCode() {
		return refereeID;
	}

	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null || obj.getClass() != getClass())
			return false;

		RefereeId other = (RefereeId) obj;
		return (refereeID == other.refereeID);
	}

	private void fromString(String str) {
		refereeID = Integer.parseInt(str);
	}
}