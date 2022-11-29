package de.regatta_hd.aquarius.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Auto-generated by: org.apache.openjpa.jdbc.meta.ReverseMappingTool$AnnotatedCodeGenerator
 */
@Entity
@Table(schema = "dbo", name = "ReportInfo")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class ReportInfo {

	@Id
	@Column(name = "ReportInfo_ID")
	private int id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Report_Comp_ID_FK")
	private Heat heat;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Report_Event_ID_FK")
	private Regatta regatta;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Report_Race_ID_FK")
	private Race race;

	@Column(name = "Report_Backend", nullable = false, length = 4)
	private String backend;

	@Column(name = "Report_Code", nullable = false, length = 16)
	private String code;

	@Column(name = "Report_Date")
	private Date date;

	@Column(name = "Report_Generated")
	@ToString.Include(rank = 10)
	private Date generated;

	@Column(name = "Report_Round")
	private Byte round;

	@Column(name = "Report_VersionMajor")
	@ToString.Include(rank = 5)
	private Byte versionMajor;

	@Column(name = "Report_VersionMinor")
	@ToString.Include(rank = 4)
	private Byte versionMinor;
}