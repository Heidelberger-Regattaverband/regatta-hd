package de.regatta_hd.aquarius.db.model;

import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Auto-generated by:
 * org.apache.openjpa.jdbc.meta.ReverseMappingTool$AnnotatedCodeGenerator
 */
@Entity
@Table(schema = "dbo", name = "Entry")
// lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class Registration {
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Entry_OwnerClub_ID_FK", nullable = false)
	@ToString.Include(rank = 9)
	private Club club;

	@OneToMany(targetEntity = HeatRegistration.class, mappedBy = "registration", cascade = CascadeType.MERGE)
	private List<HeatRegistration> heatEntries;

	@OneToMany(targetEntity = Crew.class, mappedBy = "registration", cascade = CascadeType.MERGE)
	@OrderBy("pos")
	private List<Crew> crews;

	@Basic
	@Column(name = "Entry_Bib")
	@ToString.Include(rank = 10)
	private Short bib;

	@Basic
	@Column(name = "Entry_BoatNumber")
	@ToString.Include(rank = 7)
	private Short boatNumber;

	@Basic
	@Column(name = "Entry_CancelValue")
	private byte cancelValue;

	@Basic
	@Column(name = "Entry_Comment", length = 50)
	private String comment;

	@Basic
	@Column(name = "Entry_ExternID")
	private Integer externId;

	@Basic
	@Column(name = "Entry_GroupValue")
	private Short groupValue;

	@Id
	@Column(name = "Entry_ID", columnDefinition = "int identity")
	private int id;

	@Basic
	@Column(name = "Entry_IsLate")
	private boolean isLate;

	@OneToMany(targetEntity = RegistrationLabel.class, mappedBy = "registration", cascade = CascadeType.MERGE)
	private Set<RegistrationLabel> labels;

	@Basic
	@Column(name = "Entry_Note", length = 128)
	private String note;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Entry_Event_ID_FK", nullable = false)
	private Regatta regatta;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Entry_ManualLabel_ID_FK")
	@ToString.Include(rank = 8)
	private Label label;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Entry_Race_ID_FK", nullable = false)
	private Offer offer;
}