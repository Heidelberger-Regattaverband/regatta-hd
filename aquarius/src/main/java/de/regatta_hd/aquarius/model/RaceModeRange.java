package de.regatta_hd.aquarius.model;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 */
@Entity
@Table(schema = "dbo", name = "RaceMode_Range")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class RaceModeRange {

	@Id
	@Column(name = "RMRange_ID")
	private int id;

	@Column(name = "RMRange_From")
	@ToString.Include(rank = 10)
	private int from;

	@Column(name = "RMRange_To")
	@ToString.Include(rank = 9)
	private int to;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "RMRange_RM_ID_FK", nullable = false)
	@ToString.Include(rank = 8)
	private RaceMode raceMode;

	@OneToMany(targetEntity = RaceModeDetail.class, mappedBy = "range")
	private Set<RaceModeDetail> raceModeDetails;
}