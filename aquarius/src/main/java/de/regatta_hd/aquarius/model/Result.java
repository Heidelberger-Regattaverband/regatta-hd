package de.regatta_hd.aquarius.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
public class Result {

	private static final byte FINAL = 64;

	@Id
	@Column(name = "Result_CE_ID_FK", insertable = false, updatable = false)
	private int heatRegistrationId;

	@Id
	@Column(name = "Result_SplitNr")
	@ToString.Include
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

	@Column(name = "Result_DisplayType", nullable = false, length = 1)
	@ToString.Include
	private String displayType;

	@Column(name = "Result_DisplayValue", length = 64)
	@ToString.Include
	private String displayValue;

	@Column(name = "Result_NetTime")
	@ToString.Include
	private Integer netTime;

	@Column(name = "Result_Params", length = 64)
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
		return getSplitNr() == FINAL;
	}
}