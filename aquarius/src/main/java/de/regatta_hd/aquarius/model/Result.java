package de.regatta_hd.aquarius.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import de.regatta_hd.aquarius.util.ModelUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This entity contains the result of a heat registration (boat) after the heat is finished.
 */
@Entity
@Table(schema = "dbo", name = "Result")
@IdClass(ResultId.class)
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Result {

	@Id
	@Column(name = "Result_CE_ID_FK", insertable = false, updatable = false)
	@EqualsAndHashCode.Include
	private int heatRegistrationId;

	@Id
	@Column(name = "Result_SplitNr")
	@ToString.Include
	@EqualsAndHashCode.Include
	private byte splitNr;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Result_CE_ID_FK")
	private HeatRegistration heatRegistration;

	@Column(name = "Result_Comment")
	private String comment;

	@Column(name = "Result_DayTime")
	@ToString.Include
	private Integer dayTime;

	@Column(name = "Result_Delta")
	@ToString.Include
	private Integer delta;

	@Column(name = "Result_DisplayType")
	@ToString.Include
	private String displayType;

	@Column(name = "Result_DisplayValue")
	@ToString.Include
	private String displayValue;

	@Column(name = "Result_NetTime")
	@ToString.Include
	private Integer netTime;

	@Column(name = "Result_Params")
	@ToString.Include
	private String params;

	/**
	 * The rank within a heat, starts at 1 instead of 0.
	 */
	@Column(name = "Result_Rank")
	@ToString.Include
	private byte rank;

	@Column(name = "Result_ResultType", nullable = false, length = 1)
	private String resultType;

	@Column(name = "Result_SortValue")
	@ToString.Include
	private Integer sortValue;

	public boolean isFinalResult() {
		return getSplitNr() == ModelUtils.FINAL_ROUND;
	}

	public Integer getPoints() {
		Integer points = null;
		if (getRank() > 0) {
			Race race = getHeatRegistration().getRegistration().getRace();
			int maxPoints = race.getRaceMode().getLaneCount() + 1;
			byte numRowers = race.getBoatClass().getNumRowers();
			// 1.: 5 - 1 + 4 = 8
			// 2.: 5 - 2 + 4 = 7
			points = Integer.valueOf(maxPoints - getRank() + numRowers);
		}
		return points;
	}
}