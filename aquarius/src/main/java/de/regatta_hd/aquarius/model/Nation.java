package de.regatta_hd.aquarius.model;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The nation of an {@link Athlet athlete}, {@link Club rowing club} or {@link Regatta regatta}.
 */
@Entity
@Table(schema = "dbo", name = "Nation")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class Nation {

	@Id
	@Column(name = "Nation_ID")
	private int id;

	@OneToMany(targetEntity = Athlet.class, mappedBy = "nation")
	private Set<Athlet> athlets;

	@OneToMany(targetEntity = Club.class, mappedBy = "nation")
	private Set<Club> clubs;

	@OneToMany(targetEntity = Regatta.class, mappedBy = "nation")
	private Set<Regatta> regattas;

	@Column(name = "Nation_IOC_Code")
	private String iocCode;

	@Column(name = "Nation_Name")
	@ToString.Include(rank = 10)
	private String name;

	@Column(name = "Nation_Name_German")
	@ToString.Include(rank = 8)
	private String nameGerman;

	@OneToMany(targetEntity = Referee.class, mappedBy = "nation")
	private Set<Referee> referees;
}