package de.regatta_hd.aquarius.db.model;

import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Auto-generated by:
 * org.apache.openjpa.jdbc.meta.ReverseMappingTool$AnnotatedCodeGenerator
 */
@Entity
@Table(schema = "dbo", name = "AgeClass")
@IdClass(AgeClassId.class)
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class AgeClass {
	@Basic
	@Column(name = "AgeClass_Abbr", nullable = false, length = 8)
	@ToString.Include(rank = 10)
	private String abbr;

	@Basic
	@Column(name = "AgeClass_AbbrSuffix", length = 8)
	@ToString.Include(rank = 9)
	private String abbrSuffix;

	@Basic
	@Column(name = "AgeClass_AllowYounger")
	private byte allowYounger;

	@Basic
	@Column(name = "AgeClass_Caption", nullable = false, length = 48)
	private String caption;

	@Basic
	@Column(name = "AgeClass_Gender", nullable = false, length = 1)
	@ToString.Include(rank = 8)
	private String gender;

	@Id
	@Column(name = "AgeClass_ID", columnDefinition = "int identity")
	@ToString.Include(rank = 1)
	private int id;

	@Basic
	@Column(name = "AgeClass_LW_AvgLimit")
	private Integer lwAvgLimit;

	@Basic
	@Column(name = "AgeClass_LW_CoxLowerLimit")
	private Integer lwCoxLowerLimit;

	@Basic
	@Column(name = "AgeClass_LW_CoxTolerance")
	private Integer lwCoxTolerance;

	@Basic
	@Column(name = "AgeClass_LW_UpperLimit")
	private Integer lwUpperLimit;

	@Basic
	@Column(name = "AgeClass_MaxAge")
	private byte maxAge;

	@Basic
	@Column(name = "AgeClass_MinAge")
	private byte minAge;

	@Basic
	@Column(name = "AgeClass_NumSubClasses")
	private byte numSubClasses;

	@Basic
	@Column(name = "AgeClass_Suffix", length = 16)
	private String suffix;

	@OneToMany(targetEntity = Offer.class, mappedBy = "ageClass", cascade = CascadeType.MERGE)
	private Set<Offer> offers;
}