package de.regatta_hd.aquarius.db.model;

import java.io.Serializable;

/**
 * Application identity class for: de.regatta_hd.aquarius.db.model.ReportInfo
 *
 * Auto-generated by: org.apache.openjpa.enhance.ApplicationIdTool
 */
public class ReportInfoId implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 181076043358317177L;

	static {
		// register persistent class in JVM
		try {
			Class.forName("de.regatta_hd.aquarius.db.model.ReportInfo");
		} catch (Exception e) {
		}
	}

	public int reportInfoID;

	public ReportInfoId() {
	}

	public ReportInfoId(String str) {
		fromString(str);
	}

	public int getReportInfoID() {
		return this.reportInfoID;
	}

	public void setReportInfoID(int reportInfoID) {
		this.reportInfoID = reportInfoID;
	}

	@Override
	public String toString() {
		return String.valueOf(this.reportInfoID);
	}

	@Override
	public int hashCode() {
		return this.reportInfoID;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || obj.getClass() != getClass())
			return false;

		ReportInfoId other = (ReportInfoId) obj;
		return (this.reportInfoID == other.reportInfoID);
	}

	private void fromString(String str) {
		this.reportInfoID = Integer.parseInt(str);
	}
}