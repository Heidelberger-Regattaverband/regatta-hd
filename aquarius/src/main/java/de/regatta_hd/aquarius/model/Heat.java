package de.regatta_hd.aquarius.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This entity represents a heat of a race.
 */
@Entity
@Table(schema = "dbo", name = "Comp")
@NamedEntityGraphs(@NamedEntityGraph(name = "heat-all", attributeNodes = { //
		@NamedAttributeNode(value = "entries", subgraph = "heat.entries"), //
		@NamedAttributeNode(value = "race", subgraph = "race.ageClass"), //
		@NamedAttributeNode(value = "raceModeDetail") //
}, subgraphs = { //
		@NamedSubgraph(name = "heat.entries", //
				attributeNodes = { //
						@NamedAttributeNode(value = "registration", subgraph = "registration.club"), //
						@NamedAttributeNode(value = "results") //
				}), //
		@NamedSubgraph(name = "registration.club", //
				attributeNodes = { //
						@NamedAttributeNode(value = "club"), //
						@NamedAttributeNode(value = "crews", subgraph = "crew.athlet"), //
						@NamedAttributeNode(value = "labels", subgraph = "labels.label") //
				}), //
		@NamedSubgraph(name = "labels.label", //
				attributeNodes = { //
						@NamedAttributeNode(value = "label") //
				}), //
		@NamedSubgraph(name = "crew.athlet", //
				attributeNodes = { //
						@NamedAttributeNode(value = "athlet") //
				}), //
		@NamedSubgraph(name = "race.ageClass", //
				attributeNodes = { //
						@NamedAttributeNode(value = "ageClass"), //
						@NamedAttributeNode(value = "boatClass"), //
						@NamedAttributeNode(value = "raceMode") //
				}) //
}))
//lombok
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@ToString(onlyExplicitlyIncluded = true)
public class Heat {
	private static final ResourceBundle bundle = ResourceBundle.getBundle("aquarius_messages", Locale.GERMANY);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Comp_ID")
	private int id;

	/**
	 * The {@link Race} this {@link Heat} belongs to.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Comp_Race_ID_FK")
	private Race race;

	/**
	 * The consecutive heat number within the regatta, e.g. 1, 2, 3 ... 100, 101, 102
	 */
	@Column(name = "Comp_Number")
	@ToString.Include(rank = 8)
	private short number;

	/**
	 * The consecutive heat (devision) number within the race, e.g Abteilung 1, Abteilung 2 ...
	 */
	@Column(name = "Comp_HeatNumber")
	@ToString.Include(rank = 10)
	private short devisionNumber;

	/**
	 * The time when this {@link Heat heat} is started.
	 */
	@Column(name = "Comp_DateTime")
	private Instant time;

	/**
	 * Indicates whether this {@link Heat heat} is cancelled or not.
	 */
	@Column(name = "Comp_Cancelled")
	private boolean cancelled;

	/**
	 * All assigned registrations to this heat.
	 */
	@OneToMany(targetEntity = HeatRegistration.class, mappedBy = "heat")
	@OrderBy("lane")
	private Set<HeatRegistration> entries;

	@Column(name = "Comp_Dummy")
	private boolean dummy;

	@Column(name = "Comp_GroupValue")
	private short groupValue;

	@Column(name = "Comp_Label", length = 32)
	@ToString.Include(rank = 9)
	private String roundLabel;

	@Column(name = "Comp_Locked")
	private boolean locked;

	@OneToMany(targetEntity = HeatReferee.class, mappedBy = "heat")
	private List<HeatReferee> heatReferees;

	@Column(name = "Comp_Round")
	@ToString.Include(rank = 7)
	private short round;

	@Column(name = "Comp_RoundCode", length = 8)
	@ToString.Include(rank = 6)
	private String roundCode;

	@Column(name = "Comp_State")
	private byte state;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Comp_Event_ID_FK", nullable = false)
	private Regatta regatta;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Comp_RMDetail_ID_FK")
	private RaceModeDetail raceModeDetail;

	@OneToMany(targetEntity = ReportInfo.class, mappedBy = "heat")
	private Set<ReportInfo> reportInfos;

	/**
	 * @return {@code true} if the heat is set, but not started yet.
	 */
	public boolean isStateSet() {
		return getState() == 1;
	}

	/**
	 * @return {@code true} if the heat is finished, but the result isn't official yet.
	 */
	public boolean isStateFinished() {
		return getState() == 5;
	}

	/**
	 * @return {@code true} if the heat is finished and the result is official.
	 */
	public boolean isStateOfficial() {
		return getState() == 4;
	}

	public List<HeatRegistration> getEntriesSortedByRank() {
		List<HeatRegistration> sorted = new ArrayList<>(getEntries());
		sorted.sort((entry1, entry2) -> {
			byte rank1 = entry1.getFinalResult() != null ? entry1.getFinalResult().getRank() : Byte.MAX_VALUE;
			byte rank2 = entry2.getFinalResult() != null ? entry2.getFinalResult().getRank() : Byte.MAX_VALUE;
			if (rank1 == rank2) {
				return 0;
			}
			if (rank1 == 0) {
				rank1 = Byte.MAX_VALUE;
			}
			if (rank2 == 0) {
				rank2 = Byte.MAX_VALUE;
			}
			return rank1 > rank2 ? 1 : -1;
		});
		return sorted;
	}

	public List<HeatRegistration> getEntriesSortedByLane() {
		List<HeatRegistration> sorted = new ArrayList<>(getEntries());
		sorted.sort((entry1, entry2) -> {
			if (entry1.getLane() == entry2.getLane()) {
				return 0;
			}
			return entry1.getLane() > entry2.getLane() ? 1 : -1;
		});
		return sorted;
	}

	public void setStateSet() {
		setState((byte) 1);
	}

	public String getRaceNumber() {
		return this.race.getNumber();
	}

	public String getRaceShortLabel() {
		return this.race.getShortLabel();
	}

	public String getDevisionLabel() {
		StringBuilder builder = new StringBuilder();
		builder.append(getRoundCode()).append(getRoundLabel());
		if (this.race.getAgeClass().isMasters()) {
			builder.append(", AK ").append(getGroupValueLabel());
		}
		return builder.toString();
	}

	public String getRaceLongLabel() {
		return this.race.getLongLabel();
	}

	public String getStateLabel() {
		if (isCancelled()) {
			return bundle.getString("heat.state.cancelled");
		}
		switch (getState()) {
		case 0:
			return bundle.getString("heat.state.initial");
		case 1:
			return bundle.getString("heat.state.scheduled");
		case 2:
			return bundle.getString("heat.state.started");
		case 4:
			return bundle.getString("heat.state.official");
		case 5:
			return bundle.getString("heat.state.finished");
		case 6:
			return bundle.getString("heat.state.photoFinish");
		default:
			return Byte.toString(getState());
		}
	}

	private String getGroupValueLabel() {
		switch (getGroupValue()) {
		case 0:
			return "A";
		case 4:
			return "B";
		case 8:
			return "C";
		case 12:
			return "D";
		case 16:
			return "E";
		case 20:
			return "F";
		case 24:
			return "G";
		case 28:
			return "H";
		case 32:
			return "I";
		case 36:
			return "J";
		default:
			return null;
		}
	}
}