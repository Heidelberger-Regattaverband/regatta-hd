package de.regatta_hd.aquarius.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The age class of a race.
 */
@Entity
@Table(schema = "dbo", name = "AgeClass")
@SecondaryTable(name = "HRV_AgeClass", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id") })
// lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AgeClass {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "AgeClass_ID")
	@ToString.Include(rank = 1)
	@EqualsAndHashCode.Include
	private int id;

	@Column(name = "AgeClass_Abbr")
	@ToString.Include(rank = 10)
	private String abbreviation;

	@Column(name = "AgeClass_AbbrSuffix")
	@ToString.Include(rank = 9)
	private String abbreviationSuffix;

	@Column(name = "AgeClass_AllowYounger")
	private boolean allowYounger;

	@Column(name = "AgeClass_Caption")
	private String caption;

	@Column(name = "AgeClass_Gender")
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

	@Column(name = "AgeClass_Suffix")
	private String suffix;

	@OneToMany(targetEntity = Race.class, mappedBy = "ageClass")
	@OrderBy("number")
	private List<Race> races;

	@Column(name = "distance", table = "HRV_AgeClass")
	private short distance;

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
				this.isMasters = Boolean
						.valueOf(abbrevation.equals("MM") || abbrevation.equals("MW") || abbrevation.equals("MM/W"));
			}
		}
		return this.isMasters.booleanValue();
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
				this.isOpen = Boolean.valueOf(abbrevation.equals("OFF"));
			}
		}
		return this.isOpen.booleanValue();
	}
}