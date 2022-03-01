package de.regatta_hd.aquarius.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The age class of a race.
 */
@Entity
@Table(schema = "dbo", name = "HRV_Score")
@NamedEntityGraph(name = "score-club", attributeNodes = { @NamedAttributeNode(value = "club") })
// lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Score implements Serializable {

	private static final long serialVersionUID = 1670725569568728048L;

	@Column(name = "rank")
	private short rank;

	@Column(name = "points")
	private float points;

	@Id
	@OneToOne
	@JoinColumn(name = "club_id")
	private Club club;

	@Id
	@OneToOne
	@JoinColumn(name = "event_id")
	private Regatta regatta;

	public String getClubName() {
		return getClub().getName();
	}

	public void addPoints(float addPoints) {
		this.points += addPoints;
	}
}
