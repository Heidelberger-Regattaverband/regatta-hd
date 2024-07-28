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
@NamedEntityGraph(name = Heat.GRAPH_ALL, attributeNodes = { //
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
						@NamedAttributeNode(value = "athlet", subgraph = "athlet.club") //
				}), //
		@NamedSubgraph(name = "athlet.club", //
				attributeNodes = { //
						@NamedAttributeNode(value = "club") //
				}), //
		@NamedSubgraph(name = "race.ageClass", //
				attributeNodes = { //
						@NamedAttributeNode(value = "ageClass"), //
						@NamedAttributeNode(value = "boatClass"), //
						@NamedAttributeNode(value = "raceMode") //
				}) //
})
@NamedEntityGraph(name = Heat.GRAPH_ENTRIES, attributeNodes = {
		@NamedAttributeNode(value = "entries", subgraph = "heat.entries"), //
		@NamedAttributeNode(value = "race", subgraph = "race") //
}, subgraphs = { @NamedSubgraph(name = "heat.entries", //
		attributeNodes = { //
				@NamedAttributeNode(value = "registration", subgraph = "registration"), //
				@NamedAttributeNode(value = "results") //
		}), //
		@NamedSubgraph(name = "registration", //
				attributeNodes = { //
						@NamedAttributeNode(value = "club"), //
						@NamedAttributeNode(value = "labels", subgraph = "label") //
				}), //
		@NamedSubgraph(name = "label", //
				attributeNodes = { //
						@NamedAttributeNode(value = "label") //
				}), //
		@NamedSubgraph(name = "race", //
				attributeNodes = { //
						@NamedAttributeNode(value = "ageClass"), //
						@NamedAttributeNode(value = "boatClass"), //
						@NamedAttributeNode(value = "raceMode") //
				}) //
})
//lombok
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Heat {
	private static final ResourceBundle bundle = ResourceBundle.getBundle("aquarius_messages", Locale.GERMANY);

	public static final String GRAPH_ALL = "heat-all";
	public static final String GRAPH_ENTRIES = "heat-entries";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Comp_ID")
	@EqualsAndHashCode.Include
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
	private short divisionNumber;

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

	/**
	 * Contains the round code of this heat, possible values are: "A" for a division, "R" for a single race, "F" for a
	 * final and "V" for a forerun.
	 */
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
		return getRace().getNumber();
	}

	public String getRaceShortLabel() {
		return getRace().getShortLabel();
	}

	public String getDivisionLabel() {
		StringBuilder builder = new StringBuilder();
		builder.append(getRoundCode()).append(getRoundLabel());
		if (getRace().getAgeClass().isMasters()) {
			builder.append(", AK ").append(getGroupValueLabel());
		}
		return builder.toString();
	}

	public String getRaceLongLabel() {
		return getRace().getLongLabel();
	}

	public String getStateLabel() {
		if (isCancelled()) {
			return bundle.getString("heat.state.cancelled");
		}
		return getStateLabel(getState());
	}

	private String getGroupValueLabel() {
		return switch (getGroupValue()) {
		case 0 -> "A";
		case 4 -> "B";
		case 8 -> "C";
		case 12 -> "D";
		case 16 -> "E";
		case 20 -> "F";
		case 24 -> "G";
		case 28 -> "H";
		case 32 -> "I";
		case 36 -> "J";
		default -> null;
		};
	}

	// static helpers

	public static String getStateLabel(byte state) {
		return switch (state) {
		case 0 -> bundle.getString("heat.state.initial");
		case 1 -> bundle.getString("heat.state.scheduled");
		case 2 -> bundle.getString("heat.state.started");
		case 4 -> bundle.getString("heat.state.official");
		case 5 -> bundle.getString("heat.state.finished");
		case 6 -> bundle.getString("heat.state.photoFinish");
		default -> Byte.toString(state);
		};
	}

	public static byte[] getAllowedStates() {
		return new byte[] { 0, 1, 2, 5, 4, 6 };
	}
}