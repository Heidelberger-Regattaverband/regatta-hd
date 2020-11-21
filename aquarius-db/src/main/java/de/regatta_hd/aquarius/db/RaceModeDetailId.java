package de.regatta_hd.aquarius.db;

import java.io.*;
import java.util.*;

/**
 * Application identity class for: de.regatta_hd.aquarius.db.RaceModeDetail
 *
 * Auto-generated by:
 * org.apache.openjpa.enhance.ApplicationIdTool
 */
public class RaceModeDetailId implements Serializable {
	static {
		// register persistent class in JVM
		try { Class.forName("de.regatta_hd.aquarius.db.RaceModeDetail"); }
		catch(Exception e) {}
	}

	public int rMLapID;

	public RaceModeDetailId() {
	}

	public RaceModeDetailId(String str) {
		fromString(str);
	}

	public int getRMLapID() {
		return rMLapID;
	}

	public void setRMLapID(int rMLapID) {
		this.rMLapID = rMLapID;
	}

	public String toString() {
		return String.valueOf(rMLapID);
	}

	public int hashCode() {
		return rMLapID;
	}

	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null || obj.getClass() != getClass())
			return false;

		RaceModeDetailId other = (RaceModeDetailId) obj;
		return (rMLapID == other.rMLapID);
	}

	private void fromString(String str) {
		rMLapID = Integer.parseInt(str);
	}
}