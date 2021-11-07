package de.regatta_hd.aquarius.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The age class of a race.
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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "AgeClass_ID")
	@ToString.Include(rank = 1)
	private int id;

	@Column(name = "AgeClass_Abbr", nullable = false, length = 8)
	@ToString.Include(rank = 10)
	private String abbreviation;

	@Column(name = "AgeClass_AbbrSuffix", length = 8)
	@ToString.Include(rank = 9)
	private String abbreviationSuffix;

	@Column(name = "AgeClass_AllowYounger")
	private boolean allowYounger;

	@Column(name = "AgeClass_Caption", nullable = false, length = 48)
	private String caption;

	@Column(name = "AgeClass_Gender", nullable = false, length = 1)
	@ToString.Include(rank = 8)
	private String gender;

	@Column(name = "AgeClass_LW_AvgLimit")
	private Integer lwAvgLimit;

	@Column(name = "AgeClass_LW_CoxLowerLimit")
	private Integer lwCoxLowerLimit;

	@Column(name = "AgeClass_LW_CoxTolerance")
	private Integer lwCoxTolerance;

	@Column(name = "AgeClass_LW_UpperLimit")
	private Integer lwUpperLimit;

	@Column(name = "AgeClass_MinAge")
	private byte minAge;

	@Column(name = "AgeClass_MaxAge")
	private byte maxAge;

	@Column(name = "AgeClass_NumSubClasses")
	private byte numSubClasses;

	@Column(name = "AgeClass_Suffix", length = 16)
	private String suffix;

	@OneToMany(targetEntity = Race.class, mappedBy = "ageClass")
	@OrderBy("number")
	private List<Race> races;

	@OneToOne
	@PrimaryKeyJoinColumn
	private AgeClassExt extension;

	// transient fields
	@Transient
	private Boolean isMasters;

	@Transient
	private Boolean isOpen;

	/**
	 * Indicates whether this is a master age class or not.
	 *
	 * @return {@code true} if it's a masters age class, otherwise {@code false}
	 */
	public boolean isMasters() {
		if (this.isMasters == null) {
			String abbrevation = getAbbreviation();
			if (abbrevation != null) {
				this.isMasters = abbrevation.equals("MM") || abbrevation.equals("MW") || abbrevation.equals("MM/W");
			}
		}
		return this.isMasters;
	}

	/**
	 * Indicates whether this is a open age class or not.
	 *
	 * @return {@code true} if it's a open age class, otherwise {@code false}
	 */
	public boolean isOpen() {
		if (this.isOpen == null) {
			String abbrevation = getAbbreviation();
			if (abbrevation != null) {
				this.isOpen = abbrevation.equals("OFF");
			}
		}
		return this.isOpen;
	}
}