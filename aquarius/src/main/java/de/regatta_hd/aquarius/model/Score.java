package de.regatta_hd.aquarius.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The age class of a race.
 */
@Entity
@Table(schema = "dbo", name = "Score")
@IdClass(ScoreId.class)
// lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class Score {

	@Id
	@Column(name = "club_id")
	private int clubId;

	@Id
	@Column(name = "event_id")
	private int regattaId;

	@Column(name = "points")
	private int points;

	@MapsId(value = "clubId")
	@OneToOne
	@JoinColumn(name = "club_id")
	private Club club;

	@MapsId(value = "regattaId")
	@OneToOne
	@JoinColumn(name = "event_id")
	private Regatta regatta;
}
