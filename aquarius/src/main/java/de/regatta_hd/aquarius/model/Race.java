package de.regatta_hd.aquarius.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.Table;
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
@SecondaryTable(name = "OfferExt", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id") })
@NamedEntityGraph(name = "race-to-results", //
		subgraphs = { //
				@NamedSubgraph(name = "heat.heatregs", //
						attributeNodes = { //
								@NamedAttributeNode(value = "entries", subgraph = "heatreg.results") //
						} //
				), //
				@NamedSubgraph(name = "heatreg.results", //
						attributeNodes = { //
								@NamedAttributeNode("results"), //
								@NamedAttributeNode(value = "registration", subgraph = "registration.crews") //
						} //
				), //
				@NamedSubgraph(name = "registration.crews", //
						attributeNodes = { //
								@NamedAttributeNode(value = "crews", subgraph = "crew.club"), //
								@NamedAttributeNode(value = "crews", subgraph = "crew.athlet") //
						} //
				), //
				@NamedSubgraph(name = "crew.club", //
						attributeNodes = { //
								@NamedAttributeNode("club") //
						} //
				), //
				@NamedSubgraph(name = "crew.athlet", //
				attributeNodes = { //
						@NamedAttributeNode("athlet") //
				} //
		) //
		}, //
		attributeNodes = { //
				@NamedAttributeNode(value = "heats", subgraph = "heat.heatregs"), //
				@NamedAttributeNode("ageClass"), //
				@NamedAttributeNode("boatClass"), //
				@NamedAttributeNode("raceMode") //
		} //
)
// lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class Race {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Offer_ID")
	private int id;

	@Column(name = "Offer_RaceNumber", nullable = false, length = 8)
	@ToString.Include(rank = 9)
	private String number;

	@Column(name = "Offer_LongLabel", length = 64)
	@ToString.Include(rank = 8)
	private String longLabel;

	@Column(name = "Offer_ShortLabel", length = 32)
	private String shortLabel;

	@Column(name = "Offer_Distance")
	private short distance;

	@Column(name = "Offer_IsLightweight")
	private boolean lightweight;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Offer_AgeClass_ID_FK", nullable = false)
	@ToString.Include(rank = 2)
	private AgeClass ageClass;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Offer_BoatClass_ID_FK", nullable = false)
	@ToString.Include(rank = 3)
	private BoatClass boatClass;

	@OneToMany(targetEntity = Heat.class, mappedBy = "race")
	@OrderBy("heatNumber")
	private Set<Heat> heats;

	@OneToMany(targetEntity = Cup.class, mappedBy = "race")
	private Set<Cup> cups;

	/**
	 * Contains all {@link Registration registrations} to this {@link Race offer}.
	 */
	@OneToMany(targetEntity = Registration.class, mappedBy = "race")
	@OrderBy("bib")
	private Set<Registration> registrations;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Offer_Event_ID_FK", nullable = false)
	private Regatta regatta;

	@Column(name = "Offer_BibSeed")
	private Short bibSeed;

	@Column(name = "Offer_Cancelled")
	private boolean cancelled;

	@Column(name = "Offer_Comment", length = 32)
	private String comment;

	/**
	 * Indicates whether this race is driven or not. It depends on the number of registrations for this offer.
	 */
	@Column(name = "Offer_Driven")
	private boolean driven;

	@Column(name = "Offer_EventDay")
	private Integer eventDay;

	@Column(name = "Offer_Fee")
	private double fee;

	@Column(name = "Offer_ForceDriven")
	private boolean forceDriven;

	/*
	 * 1 = Leistungsgruppe 2 = Altersklasse 3 = Leistungsgruppe und Altersklasse
	 */
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "Offer_GroupMode")
	private GroupMode groupMode;

	@Column(name = "Offer_Prize", length = 128)
	private String prize;

	@Column(name = "Offer_RaceType")
	private byte raceType;

	@Column(name = "Offer_SortValue")
	private int sortValue;

	@Column(name = "Offer_Splits")
	private Integer splits;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Offer_RaceMode_ID_FK")
	@ToString.Include(rank = 7)
	private RaceMode raceMode;

	@OneToMany(targetEntity = ReportInfo.class, mappedBy = "race")
	private Set<ReportInfo> reportInfos;

	// Second table columns

	@Column(name = "isSet", table = "OfferExt")
	private Boolean set;

	// JavaFX properties
	public ObservableBooleanValue lightweightProperty() {
		return new SimpleBooleanProperty(isLightweight());
	}

	public ObservableBooleanValue setProperty() {
		boolean isSet = this.set != null && this.set.booleanValue();
		return new SimpleBooleanProperty(isSet);
	}

	public List<Heat> getHeatsOrderedByNumber() {
		List<Heat> sorted = new ArrayList<>(getHeats());
		sorted.sort((entry1, entry2) -> entry1.getHeatNumber() > entry2.getHeatNumber() ? 1 : -1);
		return sorted;
	}

	/**
	 * @return {@code true} if the result of all {@link Heat heats} is official, otherwise {@false}.
	 */
	public boolean isOfficial() {
		return getHeats().stream().allMatch(heat -> heat.isOfficial());
	}

	public enum GroupMode {
		NONE, PERFORMANCE, AGE, PERFORMANCE_AGE
	}
}