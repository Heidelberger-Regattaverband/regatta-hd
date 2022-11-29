package de.regatta_hd.aquarius.model;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A label for a {@link Registration} or a {@link Club}.
 */
@Entity
@Table(schema = "dbo", name = "Label")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Label {

	/**
	 * Unique identifier of this {@link Label}.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Label_ID")
	@EqualsAndHashCode.Include
	private int id;

	/**
	 * An association to the corresponding {@link RegistrationLabel}.
	 */
	@OneToMany(targetEntity = RegistrationLabel.class, mappedBy = "label")
	private Set<RegistrationLabel> registrationLabels;

	/**
	 * The club to which this identifier belongs.
	 */
	@OneToMany(targetEntity = Registration.class, mappedBy = "manualLabel")
	private Set<Registration> registrations;

	/**
	 * The club to which this identifier belongs.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Label_Club_ID_FK")
	private Club club;

	/**
	 * Indicates whether this label is for a racing community or not.
	 */
	@Column(name = "Label_IsTeam")
	@ToString.Include(rank = 8)
	private Boolean community;

	/**
	 * The long label text.
	 */
	@Column(name = "Label_Long")
	@ToString.Include(rank = 9)
	private String labelLong;

	/**
	 * The short label text.
	 */
	@Column(name = "Label_Short")
	@ToString.Include(rank = 10)
	private String labelShort;

}