package de.regatta_hd.aquarius.db.model;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Auto-generated by:
 * org.apache.openjpa.jdbc.meta.ReverseMappingTool$AnnotatedCodeGenerator
 */
@Entity
@Table(schema = "dbo", name = "Crew")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class Crew {
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Crew_Athlete_ID_FK", nullable = false)
	@ToString.Include(rank = 8)
	private Athlet athlet;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Crew_Club_ID_FK", nullable = false)
	private Club club;

	@Id
	@Column(name = "Crew_ID", columnDefinition = "int identity")
	private int id;

	@Basic
	@Column(name = "Crew_IsCox")
	private boolean isCox;

	@Basic
	@Column(name = "Crew_Pos")
	@ToString.Include(rank = 10)
	private byte pos;

	@Basic
	@Column(name = "Crew_RoundFrom")
	private short roundFrom;

	@Basic
	@Column(name = "Crew_RoundTo")
	private short roundTo;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Crew_Entry_ID_FK", nullable = false)
	private Entry entry;
}