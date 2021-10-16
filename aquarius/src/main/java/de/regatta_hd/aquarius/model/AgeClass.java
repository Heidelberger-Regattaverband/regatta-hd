package de.regatta_hd.aquarius.model;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Auto-generated by: org.apache.openjpa.jdbc.meta.ReverseMappingTool$AnnotatedCodeGenerator
 */
@Entity
@Table(schema = "dbo", name = "AgeClass")
@NamedQuery(name = "AgeClass.findAll", query = "SELECT a FROM AgeClass a")
@NamedQuery(name = "AgeClass.findByAbbrevation", query = "SELECT a FROM AgeClass a WHERE a.abbreviation = :abbreviation")
// lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class AgeClass {
	@Id
	@Column(name = "AgeClass_ID")
	@ToString.Include(rank = 1)
	private int id;

	@Basic
	@Column(name = "AgeClass_Abbr", nullable = false, length = 8)
	@ToString.Include(rank = 10)
	private String abbreviation;

	@Basic
	@Column(name = "AgeClass_AbbrSuffix", length = 8)
	@ToString.Include(rank = 9)
	private String abbreviationSuffix;

	@Basic
	@Column(name = "AgeClass_AllowYounger")
	private boolean allowYounger;

	@Basic
	@Column(name = "AgeClass_Caption", nullable = false, length = 48)
	private String caption;

	@Basic
	@Column(name = "AgeClass_Gender", nullable = false, length = 1)
	@ToString.Include(rank = 8)
	private String gender;

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
	@Column(name = "AgeClass_MinAge")
	private byte minAge;

	@Basic
	@Column(name = "AgeClass_MaxAge")
	private byte maxAge;

	@Basic
	@Column(name = "AgeClass_NumSubClasses")
	private byte numSubClasses;

	@Basic
	@Column(name = "AgeClass_Suffix", length = 16)
	private String suffix;

	@OneToMany(targetEntity = Offer.class, mappedBy = "ageClass", cascade = CascadeType.MERGE)
	@OrderBy("raceNumber")
	private List<Offer> offers;

	@OneToOne(mappedBy = "ageClass")
	@PrimaryKeyJoinColumn
	private AgeClassExt extension;

	public boolean isMasters() {
		boolean isMasters = false;
		String abbr = getAbbreviation();
		if (abbr != null) {
			isMasters = abbr.equals("MM") || abbr.equals("MW") || abbr.equals("MM/W");
		}
		return isMasters;
	}
}