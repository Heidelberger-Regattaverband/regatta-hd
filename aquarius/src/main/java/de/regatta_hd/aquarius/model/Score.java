package de.regatta_hd.aquarius.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Include;

/**
 * The age class of a race.
 */
@Entity
@Table(schema = "dbo", name = "HRV_Score")
@NamedEntityGraph(name = Score.GRAPH_ALL, attributeNodes = { //
		@NamedAttributeNode(value = "club", subgraph = "club") //
}, subgraphs = { //
		@NamedSubgraph(name = "club", //
				attributeNodes = { //
						@NamedAttributeNode(value = "name") //
				}) //
})
// lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Score implements Serializable {
	private static final long serialVersionUID = 1670725569568728048L;

	public static final String GRAPH_ALL = "score-all";

	@Id
	@OneToOne
	@JoinColumn(name = "club_id")
	@Include
	private Club club;

	@Id
	@OneToOne
	@JoinColumn(name = "event_id")
	private Regatta regatta;

	@Column(name = "rank")
	@Include
	private short rank;

	@Column(name = "points")
	@Include
	private float points;

	public String getClubName() {
		return getClub().getName();
	}

	public void addPoints(float addPoints) {
		this.points += addPoints;
	}
}
