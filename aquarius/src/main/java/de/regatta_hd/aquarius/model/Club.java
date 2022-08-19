package de.regatta_hd.aquarius.model;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

	/**
	 * The name of the rowing club.
	 */
	@Column(name = "Club_Name")
	@ToString.Include
	private String name;

	/**
	 * An abbreviation of the rowing club.
	 */
	@Column(name = "Club_Abbr")
	private String abbreviation;

	/**
	 * A very short abbreviation of the rowing club.
	 */
	@Column(name = "Club_UltraAbbr")
	private String ultraAbbr;

	/**
	 * The city where the rowing club is based.
	 */
	@Column(name = "Club_City")
	private String city;

	/**
	 * All registered {@link Athlet athlets} of this rowing club.
	 */
	@OneToMany(targetEntity = Athlet.class, mappedBy = "club")
	private Set<Athlet> athlets;

	/**
	 * An optional discount this club gets.
	 */
	@Column(name = "Club_Discount")
	private Double discount;

	/**
	 * The nationality of the rowing club.
	 */
	@ManyToOne
	@JoinColumn(name = "Club_Nation_ID_FK")
	private Nation nation;

	@Column(name = "Club_ExternID")
	private Integer externID;

	@OneToMany(targetEntity = Crew.class, mappedBy = "club")
	private Set<Crew> crews;

	/**
	 * All {@link Registration registrations} of this {@link Club rowing club} to available {@link Race offers}.
	 */
	@OneToMany(targetEntity = Registration.class, mappedBy = "club")
	private Set<Registration> registrations;

	@OneToMany(targetEntity = Regatta.class, mappedBy = "club")
	private Set<Regatta> regattas;

	@OneToMany(targetEntity = Label.class, mappedBy = "club")
	private Set<Label> labels;

}