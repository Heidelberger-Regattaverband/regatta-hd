package de.regatta_hd.aquarius.db.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Auto-generated by: org.apache.openjpa.jdbc.meta.ReverseMappingTool$AnnotatedCodeGenerator
 */
@Entity
@Table(schema = "dbo", name = "Nation")
@IdClass(de.regatta_hd.aquarius.db.model.NationId.class)
public class Nation {
	@OneToMany(targetEntity = de.regatta_hd.aquarius.db.model.Athlet.class, mappedBy = "nation", cascade = CascadeType.MERGE)
	private Set<Athlet> athlets = new HashSet<>();

	@OneToMany(targetEntity = de.regatta_hd.aquarius.db.model.Club.class, mappedBy = "nation", cascade = CascadeType.MERGE)
	private Set<Club> clubs = new HashSet<>();

	@OneToMany(targetEntity = de.regatta_hd.aquarius.db.model.Event.class, mappedBy = "nation", cascade = CascadeType.MERGE)
	private Set<Event> events = new HashSet<>();

	@Id
	@Column(name = "Nation_ID", columnDefinition = "int identity")
	private int nationID;

	@Basic
	@Column(name = "Nation_IOC_Code", length = 5)
	private String nationIOCCode;

	@Basic
	@Column(name = "Nation_Name", length = 64)
	private String nationName;

	@Basic
	@Column(name = "Nation_Name_German", length = 64)
	private String nationNameGerman;

	@OneToMany(targetEntity = de.regatta_hd.aquarius.db.model.Referee.class, mappedBy = "nation", cascade = CascadeType.MERGE)
	private Set<Referee> referees = new HashSet<>();

	public Nation() {
	}

	public Nation(int nationID) {
		this.nationID = nationID;
	}

	public Set<Athlet> getAthlets() {
		return this.athlets;
	}

	public void setAthlets(Set<Athlet> athlets) {
		this.athlets = athlets;
	}

	public Set<Club> getClubs() {
		return this.clubs;
	}

	public void setClubs(Set<Club> clubs) {
		this.clubs = clubs;
	}

	public Set<Event> getEvents() {
		return this.events;
	}

	public void setEvents(Set<Event> events) {
		this.events = events;
	}

	public int getNationID() {
		return this.nationID;
	}

	public void setNationID(int nationID) {
		this.nationID = nationID;
	}

	public String getNationIOCCode() {
		return this.nationIOCCode;
	}

	public void setNationIOCCode(String nationIOCCode) {
		this.nationIOCCode = nationIOCCode;
	}

	public String getNationName() {
		return this.nationName;
	}

	public void setNationName(String nationName) {
		this.nationName = nationName;
	}

	public String getNationNameGerman() {
		return this.nationNameGerman;
	}

	public void setNationNameGerman(String nationNameGerman) {
		this.nationNameGerman = nationNameGerman;
	}

	public Set<Referee> getReferees() {
		return this.referees;
	}

	public void setReferees(Set<Referee> referees) {
		this.referees = referees;
	}
}