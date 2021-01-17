package de.regatta_hd.aquarius.db.model;

import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
@Table(schema = "dbo", name = "BoatClass")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class BoatClass {
	@Basic
	@Column(name = "BoatClass_Abbr", nullable = false, length = 8)
	@ToString.Include(rank = 10)
	private String abbr;

	@Basic
	@Column(name = "BoatClass_Caption", nullable = false, length = 50)
	@ToString.Include(rank = 9)
	private String caption;

	@Basic
	@Column(name = "BoatClass_Coxed")
	private byte coxed;

	@Id
	@Column(name = "BoatClass_ID", columnDefinition = "int identity")
	@ToString.Include(rank = 1)
	private int id;

	@Basic
	@Column(name = "BoatClass_NumRowers")
	private byte numRowers;

	@OneToMany(targetEntity = Offer.class, mappedBy = "boatClass", cascade = CascadeType.MERGE)
	private Set<Offer> offers;
}