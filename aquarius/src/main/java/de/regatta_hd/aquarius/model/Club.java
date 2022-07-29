package de.regatta_hd.aquarius.model;

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
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The entity contains data about a club.
 */
@Entity
@Table(schema = "dbo", name = "Club")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Club {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Club_ID")
	@EqualsAndHashCode.Include
	private int id;

	@OneToMany(targetEntity = Athlet.class, mappedBy = "club")
	private Set<Athlet> athlets;

	@Column(name = "Club_Abbr", length = 50)
	private String abbreviation;

	@Column(name = "Club_City", length = 64)
	private String city;

	@Column(name = "Club_Discount")
	private Double discount;

	@Column(name = "Club_ExternID")
	private Integer externID;

	@Column(name = "Club_Name", length = 128)
	@ToString.Include
	private String name;

	@Column(name = "Club_UltraAbbr", length = 16)
	private String ultraAbbr;

	@OneToMany(targetEntity = Crew.class, mappedBy = "club")
	private Set<Crew> crews;

	/**
	 * Contains all {@link Registration registrations} of this {@link Club club} to available {@link Race offers}.
	 */
	@OneToMany(targetEntity = Registration.class, mappedBy = "club")
	private List<Registration> registrations;

	@OneToMany(targetEntity = Regatta.class, mappedBy = "club")
	private Set<Regatta> regattas;

	@OneToMany(targetEntity = Label.class, mappedBy = "club")
	private Set<Label> labels;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Club_Nation_ID_FK")
	private Nation nation;
}