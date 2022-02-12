package de.regatta_hd.aquarius.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This entity represents a heat of a race.
 */
@Entity
@Table(schema = "dbo", name = "Comp")
//lombok
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Heat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Comp_ID")
	private int id;

	@Column(name = "Comp_HeatNumber")
	@ToString.Include(rank = 10)
	private short heatNumber;

	@Column(name = "Comp_Cancelled")
	private boolean cancelled;

	@Column(name = "Comp_DateTime")
	private Date dateTime;

	@Column(name = "Comp_Dummy")
	private boolean dummy;

	@OneToMany(targetEntity = HeatRegistration.class, mappedBy = "heat")
	@OrderBy("lane")
	private Set<HeatRegistration> entries;

	@Column(name = "Comp_GroupValue")
	private short groupValue;

	@Column(name = "Comp_Label", length = 32)
	@ToString.Include(rank = 9)
	private String label;

	@Column(name = "Comp_Locked")
	private boolean locked;

	@Column(name = "Comp_Number")
	@ToString.Include(rank = 8)
	private short number;

	@OneToMany(targetEntity = HeatReferee.class, mappedBy = "heat")
	private List<HeatReferee> heatReferees;

	@Column(name = "Comp_Round")
	@ToString.Include(rank = 7)
	private Short round;

	@Column(name = "Comp_RoundCode", length = 8)
	@ToString.Include(rank = 6)
	private String roundCode;

	@Column(name = "Comp_State")
	private byte state;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Comp_Event_ID_FK", nullable = false)
	private Regatta regatta;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Comp_Race_ID_FK")
	private Race race;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Comp_RMDetail_ID_FK")
	private RaceModeDetail raceModeDetail;

	@OneToMany(targetEntity = ReportInfo.class, mappedBy = "heat")
	private Set<ReportInfo> reportInfos;

	/**
	 * @return {@code true} if the heat is set, but not started yet.
	 */
	public boolean isSet() {
		return getState() == 1;
	}

	/**
	 * @return {@code true} if the heat is finished, but the result isn't official yet.
	 */
	public boolean isFinished() {
		return getState() == 5;
	}

	/**
	 * @return {@code true} if the heat is finished and the result is official.
	 */
	public boolean isOfficial() {
		return getState() == 4;
	}

	public List<HeatRegistration> getHeatRegistrationsOrderedByRank() {
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
}