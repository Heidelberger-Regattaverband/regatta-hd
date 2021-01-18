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
@Table(schema = "dbo", name = "EntryLabel")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class EntryLabel {
	@Basic
	@Column(name = "EL_IsClubMultiNOC")
	private boolean isClubMultiNOC;

	@Basic
	@Column(name = "EL_IsCrewClubMultiNOC")
	private boolean isCrewClubMultiNOC;

	@Basic
	@Column(name = "EL_RoundFrom")
	private short roundFrom;

	@Basic
	@Column(name = "EL_RoundTo")
	private short roundTo;

	@Id
	@Column(name = "EL_ID", columnDefinition = "int identity")
	private int id;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "EL_Entry_ID_FK", nullable = false)
	private Entry entry;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "EL_Label_ID_FK", nullable = false)
	@ToString.Include
	private Label label;
}