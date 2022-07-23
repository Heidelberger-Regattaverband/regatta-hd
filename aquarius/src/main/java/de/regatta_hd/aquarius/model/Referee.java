package de.regatta_hd.aquarius.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A {@link Referee} of an {@link Heat}.
 */
@Entity
@Table(schema = "dbo", name = "Referee")
//lombok
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Referee {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Referee_ID")
	@Include
	private int id;

	@Column(name = "Referee_LastName")
	private String lastName;

	@Column(name = "Referee_FirstName")
	private String firstName;

	@Column(name = "Referee_City")
	private String city;

	@Column(name = "Referee_ExternID")
	private Long externID;

	@Column(name = "Referee_LicenceState")
	private boolean licenceState;

	@OneToMany(targetEntity = HeatReferee.class, mappedBy = "referee")
	private List<HeatReferee> heatReferees;

	@OneToMany(targetEntity = Regatta.class, mappedBy = "referee")
	private List<Regatta> regattas;

	@ManyToMany(targetEntity = Regatta.class, mappedBy = "referees")
	private List<Regatta> regattas2;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Referee_Nation_ID_FK")
	private Nation nation;

	// JavaFX properties
	public BooleanProperty activeProperty() {
		BooleanProperty property = new SimpleBooleanProperty(this.licenceState);
		property.addListener((observable, oldValue, newValue) -> this.licenceState = newValue.booleanValue());
		return property;
	}
}