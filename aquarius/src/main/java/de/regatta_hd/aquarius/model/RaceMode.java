package de.regatta_hd.aquarius.model;

import java.util.Set;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Auto-generated by: org.apache.openjpa.jdbc.meta.ReverseMappingTool$AnnotatedCodeGenerator
 */
@Entity
@Table(schema = "dbo", name = "RaceMode")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RaceMode {

	@Id
	@Column(name = "RaceMode_ID")
	@EqualsAndHashCode.Include
	private int id;

	@OneToMany(targetEntity = Regatta.class, mappedBy = "raceMode")
	private Set<Regatta> events;

	@OneToMany(targetEntity = Race.class, mappedBy = "raceMode")
	private Set<Race> offers;

	@Column(name = "RaceMode_IsSystemMode")
	@ToString.Include(rank = 8)
	private boolean isSystemMode;

	@Basic
	@Column(name = "RaceMode_LaneCount")
	@ToString.Include(rank = 9)
	private short laneCount;

	@OneToMany(targetEntity = RaceModeRange.class, mappedBy = "raceMode")
	private Set<RaceModeRange> ranges;

	@Basic
	@Column(name = "RaceMode_Title", nullable = false, length = 32)
	@ToString.Include(rank = 10)
	private String title;
}