package de.regatta_hd.aquarius.model;

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
	private Short heatNumber;

	@Column(name = "Comp_Cancelled")
	private boolean cancelled;

	@Column(name = "Comp_DateTime")
	private Date dateTime;

	@Column(name = "Comp_Dummy")
	private boolean dummy;

	@OneToMany(targetEntity = HeatRegistration.class, mappedBy = "heat")
	@OrderBy("lane")
	private List<HeatRegistration> entries;

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

	public List<HeatRegistration> getHeatRegistrationsOrderedByRank() {
		List<HeatRegistration> sorted = getEntries();
		sorted.sort((entry1, entry2) -> {
			Result result1 = entry1.getFinalResult();
			Result result2 = entry2.getFinalResult();
			if (result1 != null && result2 != null) {
				return result1.getRank() > result2.getRank() ? 1 : -1;
			}
			return 0;
		});
		return sorted;
	}
}