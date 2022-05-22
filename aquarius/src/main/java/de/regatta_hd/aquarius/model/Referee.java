package de.regatta_hd.aquarius.model;

import java.util.List;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Auto-generated by: org.apache.openjpa.jdbc.meta.ReverseMappingTool$AnnotatedCodeGenerator
 */
@Entity
@Table(schema = "dbo", name = "Referee")
//lombok
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class Referee {
	@Id
	@Column(name = "Referee_ID", columnDefinition = "int identity")
	private int id;

	@OneToMany(targetEntity = HeatReferee.class, mappedBy = "referee", cascade = CascadeType.MERGE)
	private List<HeatReferee> heatReferees;

	@OneToMany(targetEntity = Regatta.class, mappedBy = "referee", cascade = CascadeType.MERGE)
	private List<Regatta> regattas;

	@ManyToMany(targetEntity = Regatta.class, mappedBy = "referees", cascade = CascadeType.MERGE)
	private List<Regatta> regattas2;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "Referee_Nation_ID_FK")
	private Nation nation;

	@Column(name = "Referee_City", length = 32)
	private String city;

	@Column(name = "Referee_ExternID")
	private Long externID;

	@Basic
	@Column(name = "Referee_FirstName", nullable = false, length = 32)
	private String firstName;

	@Column(name = "Referee_LastName", nullable = false, length = 64)
	private String lastName;

	@Column(name = "Referee_LicenceState")
	private boolean licenceState;

	public BooleanProperty activeProperty() {
		BooleanProperty property = new SimpleBooleanProperty(this.licenceState);
		property.addListener((observable, oldValue, newValue) -> this.licenceState = newValue.booleanValue());
		return property;
	}
}