package de.regatta_hd.aquarius.db.model;

import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(schema = "dbo", name = "Club")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class Club {
	@OneToMany(targetEntity = Athlet.class, mappedBy = "club", cascade = CascadeType.MERGE)
	private Set<Athlet> athlets;

	@Basic
	@Column(name = "Club_Abbr", length = 50)
	private String abbr;

	@Basic
	@Column(name = "Club_City", length = 64)
	private String city;

	@Basic
	@Column(name = "Club_Discount")
	private Double discount;

	@Basic
	@Column(name = "Club_ExternID")
	private Integer externID;

	@Id
	@Column(name = "Club_ID", columnDefinition = "int identity")
	private int id;

	@Basic
	@Column(name = "Club_Name", length = 128)
	@ToString.Include
	private String name;

	@Basic
	@Column(name = "Club_UltraAbbr", length = 16)
	private String ultraAbbr;

	@OneToMany(targetEntity = Crew.class, mappedBy = "club", cascade = CascadeType.MERGE)
	private Set<Crew> crews;

	@OneToMany(targetEntity = Entry.class, mappedBy = "club", cascade = CascadeType.MERGE)
	private Set<Entry> entries;

	@OneToMany(targetEntity = Regatta.class, mappedBy = "club", cascade = CascadeType.MERGE)
	private Set<Regatta> regattas;

	@OneToMany(targetEntity = Label.class, mappedBy = "club", cascade = CascadeType.MERGE)
	private Set<Label> labels;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Club_Nation_ID_FK")
	private Nation nation;
}