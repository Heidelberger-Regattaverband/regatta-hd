package de.regatta_hd.aquarius.model;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
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
@Table(schema = "dbo", name = "ProgressionRule")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class ProgressionRule {
	@Basic
	@Column(name = "PR_Arg")
	private Byte arg;

	@Basic
	@Column(name = "PR_Option")
	private Byte option;

	@Basic
	@Column(name = "PR_Order")
	private short order;

	@Basic
	@Column(name = "PR_Type", nullable = false, length = 1)
	private String type;

	@Id
	@Column(name = "PR_ID", columnDefinition = "int identity")
	private int id;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "PR_RMLap_ID_FK", nullable = false)
	private RaceModeDetail raceModeDetail;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "PR_Src_Lap_ID_FK")
	private RaceModeDetail raceModeDetail2;
}