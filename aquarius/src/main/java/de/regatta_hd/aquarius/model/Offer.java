package de.regatta_hd.aquarius.model;

import java.util.List;
import java.util.Set;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * An offer for a race.
 */
@Entity
@Table(schema = "dbo", name = "Offer")
// lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class Offer {

	@Id
	@Column(name = "Offer_ID")
	private int id;

	@Column(name = "Offer_RaceNumber", nullable = false, length = 8)
	@ToString.Include(rank = 9)
	private String raceNumber;

	@Column(name = "Offer_LongLabel", length = 64)
	@ToString.Include(rank = 8)
	private String longLabel;

	@Column(name = "Offer_ShortLabel", length = 32)
	private String shortLabel;

	@Column(name = "Offer_Distance")
	private short distance;

	@Column(name = "Offer_IsLightweight")
	private boolean lightweight;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Offer_AgeClass_ID_FK", nullable = false)
	@ToString.Include(rank = 2)
	private AgeClass ageClass;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Offer_BoatClass_ID_FK", nullable = false)
	@ToString.Include(rank = 3)
	private BoatClass boatClass;

	@OneToMany(targetEntity = Heat.class, mappedBy = "offer", cascade = CascadeType.MERGE)
	@OrderBy("heatNumber")
	private List<Heat> heats;

	@OneToMany(targetEntity = Cup.class, mappedBy = "offer", cascade = CascadeType.MERGE)
	private Set<Cup> cups;

	/**
	 * Contains all {@link Registration registrations} to this {@link Offer offer}.
	 */
	@OneToMany(targetEntity = Registration.class, mappedBy = "offer", cascade = CascadeType.MERGE)
	@OrderBy("bib")
	private Set<Registration> registrations;

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

	/**
	 * Indicates whether this race is driven or not. It depends on the number of registrations for this offer.
	 */
	@Basic
	@Column(name = "Offer_Driven")
	private boolean driven;

	@Basic
	@Column(name = "Offer_EventDay")
	private Integer eventDay;

	@Column(name = "Offer_Fee")
	private double fee;

	@Basic
	@Column(name = "Offer_ForceDriven")
	private boolean forceDriven;

	/*
	 * 1 = Leistungsgruppe 2 = Altersklasse 3 = Leistungsgruppe und Altersklasse
	 */
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "Offer_GroupMode")
	private GroupMode groupMode;

	@Basic
	@Column(name = "Offer_Prize", length = 128)
	private String prize;

	@Basic
	@Column(name = "Offer_RaceType")
	private byte raceType;

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

	// JavaFX properties
	@Transient
	private SimpleBooleanProperty lightweightProp;

	public ObservableBooleanValue lightweightProperty() {
		if (this.lightweightProp == null) {
			this.lightweightProp = new SimpleBooleanProperty(this.lightweight);
		}
		return this.lightweightProp;
	}

	public List<Heat> getHeatsOrderedByNumber() {
		List<Heat> sorted = getHeats();
		sorted.sort((entry1, entry2) -> {
			Short result1 = entry1.getHeatNumber();
			Short result2 = entry2.getHeatNumber();
			if (result1 != null && result2 != null) {
				return result1 > result2 ? 1 : -1;
			}
			return 0;
		});
		return sorted;
	}

	public enum GroupMode {
		NONE,
		PERFORMANCE,
		AGE,
		PERFORMANCE_AGE
	}
}