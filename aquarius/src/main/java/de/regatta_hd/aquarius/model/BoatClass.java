package de.regatta_hd.aquarius.model;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The boat class of a {@link Race}.
 */
@Entity
@Table(schema = "dbo", name = "BoatClass")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class BoatClass {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "BoatClass_ID")
	@ToString.Include(rank = 1)
	private int id;

	@Column(name = "BoatClass_Abbr", nullable = false, length = 8)
	@ToString.Include(rank = 10)
	private String abbreviation;

	@Column(name = "BoatClass_Caption", nullable = false, length = 50)
	@ToString.Include(rank = 9)
	private String caption;

	@Column(name = "BoatClass_Coxed")
	private boolean coxed;

	@Column(name = "BoatClass_NumRowers")
	private byte numRowers;

	@OneToMany(targetEntity = Race.class, mappedBy = "boatClass")
	@OrderBy("raceNumber")
	private Set<Race> races;
}