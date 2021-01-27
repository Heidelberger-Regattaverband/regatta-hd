package de.regatta_hd.aquarius.db.model;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Auto-generated by:
 * org.apache.openjpa.jdbc.meta.ReverseMappingTool$AnnotatedCodeGenerator
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

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Result_CE_ID_FK")
	private HeatEntry heatEntry;

	@Id
	@Column(name = "Result_CE_ID_FK", insertable = false, updatable = false)
	private int CEIDFK;

	@Basic
	@Column(name = "Result_Comment")
	private String comment;

	@Basic
	@Column(name = "Result_DayTime")
	@ToString.Include
	private Integer dayTime;

	@Basic
	@Column(name = "Result_Delta")
	@ToString.Include
	private Integer delta;

	@Basic
	@Column(name = "Result_DisplayType", nullable = false, length = 1)
	@ToString.Include
	private String displayType;

	@Basic
	@Column(name = "Result_DisplayValue", length = 64)
	@ToString.Include
	private String displayValue;

	@Basic
	@Column(name = "Result_NetTime")
	@ToString.Include
	private Integer netTime;

	@Basic
	@Column(name = "Result_Params", length = 64)
	@ToString.Include
	private String params;

	@Basic
	@Column(name = "Result_Rank")
	@ToString.Include
	private Byte rank;

	@Basic
	@Column(name = "Result_ResultType", nullable = false, length = 1)
	private String resultType;

	@Basic
	@Column(name = "Result_SortValue")
	@ToString.Include
	private Integer sortValue;

	@Id
	@Column(name = "Result_SplitNr")
	@ToString.Include
	private byte splitNr;

	public boolean isFinalResult() {
		return getSplitNr() == FINAL;
	}
}