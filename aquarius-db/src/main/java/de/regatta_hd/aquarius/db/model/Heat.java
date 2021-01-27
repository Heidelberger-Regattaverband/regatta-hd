package de.regatta_hd.aquarius.db.model;

import java.util.Date;
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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a heat of a race.
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
	@Basic
	@Column(name = "Comp_Cancelled")
	private boolean cancelled;

	@Basic
	@Column(name = "Comp_DateTime", columnDefinition = "datetime")
	private Date dateTime;

	@Basic
	@Column(name = "Comp_Dummy")
	private boolean dummy;

	@OneToMany(targetEntity = CompEntries.class, mappedBy = "heat", cascade = CascadeType.MERGE)
	@OrderBy("lane")
	private List<CompEntries> compEntries;

	@Basic
	@Column(name = "Comp_GroupValue")
	private short groupValue;

	@Basic
	@Column(name = "Comp_HeatNumber")
	@ToString.Include(rank = 10)
	private Short heatNumber;

	@Id
	@Column(name = "Comp_ID", columnDefinition = "int identity")
	private int id;

	@Basic
	@Column(name = "Comp_Label", length = 32)
	@ToString.Include(rank = 9)
	private String label;

	@Basic
	@Column(name = "Comp_Locked")
	private boolean locked;

	@Basic
	@Column(name = "Comp_Number")
	@ToString.Include(rank = 8)
	private short number;

	@OneToMany(targetEntity = CompReferee.class, mappedBy = "heat", cascade = CascadeType.MERGE)
	private Set<CompReferee> compReferees;

	@Basic
	@Column(name = "Comp_Round")
	@ToString.Include(rank = 7)
	private Short round;

	@Basic
	@Column(name = "Comp_RoundCode", length = 8)
	@ToString.Include(rank = 6)
	private String roundCode;

	@Basic
	@Column(name = "Comp_State")
	private byte state;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Comp_Event_ID_FK", nullable = false)
	private Regatta regatta;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Comp_Race_ID_FK")
	private Offer offer;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Comp_RMDetail_ID_FK")
	private RaceModeDetail raceModeDetail;

	@OneToMany(targetEntity = ReportInfo.class, mappedBy = "heat", cascade = CascadeType.MERGE)
	private Set<ReportInfo> reportInfos;

	public List<CompEntries> getCompEntriesOrderedByRank() {
		List<CompEntries> sorted = getCompEntries();
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