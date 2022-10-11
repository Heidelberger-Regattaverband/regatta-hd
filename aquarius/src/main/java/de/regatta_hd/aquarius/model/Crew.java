package de.regatta_hd.aquarius.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.Table;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A member of a boat crew.
 */
@Entity
@Table(schema = "dbo", name = "Crew")
@NamedEntityGraph(name = "crew-all", attributeNodes = { @NamedAttributeNode("athlet"), @NamedAttributeNode("club") })
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Crew {

	/**
	 * The unique identifier of this {@link Crew crew}.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Crew_ID")
	@EqualsAndHashCode.Include
	private int id;

	/**
	 * Contains the {@link Registration registration} this crew belongs to.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Crew_Entry_ID_FK", nullable = false)
	private Registration registration;

	/**
	 * The athlete who represents this crew.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Crew_Athlete_ID_FK", nullable = false)
	@ToString.Include(rank = 8)
	private Athlet athlet;

	/**
	 * The club the crew belongs to.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Crew_Club_ID_FK", nullable = false)
	private Club club;

	/**
	 * The position in the boat.
	 */
	@Column(name = "Crew_Pos")
	@ToString.Include(rank = 10)
	private byte pos;

	/**
	 * Indicates whether this is the cox of the boat or not.
	 */
	@Column(name = "Crew_IsCox")
	private boolean cox;

	@Column(name = "Crew_RoundFrom")
	private short roundFrom;

	@Column(name = "Crew_RoundTo")
	private short roundTo;

	public String getName() {
		return this.athlet.getLastName() + ", " + this.athlet.getFirstName();
	}

	// JavaFX properties
	public ObservableBooleanValue coxProperty() {
		return new SimpleBooleanProperty(isCox());
	}

}