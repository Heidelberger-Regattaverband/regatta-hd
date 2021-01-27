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
@Table(schema = "dbo", name = "Offer")
// lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class Offer {
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Offer_AgeClass_ID_FK", nullable = false)
	@ToString.Include(rank = 2)
	private AgeClass ageClass;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Offer_BoatClass_ID_FK", nullable = false)
	@ToString.Include(rank = 3)
	private BoatClass boatClass;

	@OneToMany(targetEntity = Comp.class, mappedBy = "offer", cascade = CascadeType.MERGE)
	@OrderBy("heatNumber")
	private List<Comp> comps;

	@OneToMany(targetEntity = Cup.class, mappedBy = "offer", cascade = CascadeType.MERGE)
	private Set<Cup> cups;

	@OneToMany(targetEntity = Entry.class, mappedBy = "offer", cascade = CascadeType.MERGE)
	@OrderBy("bib")
	private Set<Entry> entries;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Offer_Event_ID_FK", nullable = false)
	private Regatta regatta;

	@Basic
	@Column(name = "Offer_BibSeed")
	private Short bibSeed;

	@Basic
	@Column(name = "Offer_Cancelled")
	private boolean cancelled;

	@Basic
	@Column(name = "Offer_Comment", length = 32)
	private String comment;

	@Basic
	@Column(name = "Offer_Distance")
	private short distance;

	/**
	 * Indicates whether this race is driven or not. It depends on the number of
	 * registrations for this offer.
	 */
	@Basic
	@Column(name = "Offer_Driven")
	private boolean driven;

	@Basic
	@Column(name = "Offer_EventDay")
	private Integer eventDay;

	@Basic
	@Column(name = "Offer_Fee", columnDefinition = "smallmoney")
	private double fee;

	@Basic
	@Column(name = "Offer_ForceDriven")
	private boolean forceDriven;

	@Basic
	@Column(name = "Offer_GroupMode")
	private byte groupMode;

	@Id
	@Column(name = "Offer_ID", columnDefinition = "int identity")
	private int id;

	@Basic
	@Column(name = "Offer_IsLightweight")
	private boolean lightweight;

	@Basic
	@Column(name = "Offer_LongLabel", length = 64)
	@ToString.Include(rank = 8)
	private String longLabel;

	@Basic
	@Column(name = "Offer_Prize", length = 128)
	private String prize;

	@Basic
	@Column(name = "Offer_RaceNumber", nullable = false, length = 8)
	@ToString.Include(rank = 9)
	private String raceNumber;

	@Basic
	@Column(name = "Offer_RaceType")
	private byte raceType;

	@Basic
	@Column(name = "Offer_ShortLabel", length = 32)
	private String shortLabel;

	@Basic
	@Column(name = "Offer_SortValue")
	private int sortValue;

	@Basic
	@Column(name = "Offer_Splits")
	private Integer splits;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Offer_RaceMode_ID_FK")
	@ToString.Include(rank = 7)
	private RaceMode raceMode;

	@OneToMany(targetEntity = ReportInfo.class, mappedBy = "offer", cascade = CascadeType.MERGE)
	private Set<ReportInfo> reportInfos;
}