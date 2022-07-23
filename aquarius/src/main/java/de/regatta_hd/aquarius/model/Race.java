package de.regatta_hd.aquarius.model;

import java.util.Set;
import java.util.stream.Stream;

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
import jakarta.persistence.NamedEntityGraphs;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.Table;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * An offer for a race.
 */
@Entity
@Table(schema = "dbo", name = "Offer")
@SecondaryTable(name = "HRV_Offer", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id") })
@NamedEntityGraphs({ @NamedEntityGraph(name = Race.GRAPH_RESULTS, //
		subgraphs = { //
				@NamedSubgraph(name = "heat.heatregs", //
						attributeNodes = { @NamedAttributeNode(value = "entries", subgraph = "heatreg.results") } //
				), //
				@NamedSubgraph(name = "heatreg.results", //
						attributeNodes = { //
								@NamedAttributeNode("results"), //
								@NamedAttributeNode(value = "registration", subgraph = "registration.crews") //
						} //
				), //
				@NamedSubgraph(name = "registration.crews", //
						attributeNodes = { @NamedAttributeNode(value = "crews", subgraph = "crew.club") } //
				), //
				@NamedSubgraph(name = "crew.club", //
						attributeNodes = { @NamedAttributeNode("club"), @NamedAttributeNode("athlet") } //
				) }, //
		attributeNodes = { //
				@NamedAttributeNode(value = "heats", subgraph = "heat.heatregs"), //
				@NamedAttributeNode("ageClass"), //
				@NamedAttributeNode("boatClass"), //
				@NamedAttributeNode("raceMode"), //
				@NamedAttributeNode("registrations") //
		} //
), @NamedEntityGraph(name = Race.GRAPH_CLUBS, //
		subgraphs = { @NamedSubgraph(name = "registration.club", attributeNodes = { @NamedAttributeNode("club") }) }, //
		attributeNodes = { @NamedAttributeNode(value = "registrations", subgraph = "registration.club") }) })
// lombok
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Race {

	public static final String GRAPH_CLUBS = "race-registrations-clubs";
	public static final String GRAPH_RESULTS = "race-heats-heatreg-results";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Offer_ID")
	@Include
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
	@OrderBy("devisionNumber")
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
	 * Indicates whether this {@link Race} is driven or not. It depends on the number of registrations for race.
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

	@Column(name = "isSet", table = "HRV_Offer")
	private Boolean set;

	// JavaFX properties
	public ObservableBooleanValue lightweightProperty() {
		return new SimpleBooleanProperty(isLightweight());
	}

	public ObservableBooleanValue setProperty() {
		return new SimpleBooleanProperty(isSet());
	}

	/**
	 * Returns a stream with active registrations, cancelled registrations are removed.
	 *
	 * @return a {@link Stream} with active registrations.
	 */
	public Stream<Registration> getActiveRegistrations() {
		return getRegistrations().stream().filter(registration -> !registration.isCancelled());
	}

	/**
	 * Returns a list of all driven heats, cancelled or empty heats are excluded.
	 *
	 * @return a list of driven heats
	 */
	public Stream<Heat> getDrivenHeats() {
		return getSortedHeats().filter(heat -> !heat.isCancelled());
	}

	/**
	 * @return {@code true} if the result of all driven {@link Heat heats} are official, otherwise {@code false}.
	 */
	public boolean isOfficial() {
		return getDrivenHeats().allMatch(Heat::isStateOfficial);
	}

	/**
	 * Indicates whether this race was set or not.
	 *
	 * @return {@code true} if race was set, otherwise {@code false}
	 */
	public boolean isSet() {
		return this.set != null && this.set.booleanValue();
	}

	private Stream<Heat> getSortedHeats() {
		return getHeats().stream()
				.sorted((entry1, entry2) -> entry1.getDevisionNumber() > entry2.getDevisionNumber() ? 1 : -1);
	}

	public enum GroupMode {
		NONE, PERFORMANCE, AGE, PERFORMANCE_AGE
	}
}