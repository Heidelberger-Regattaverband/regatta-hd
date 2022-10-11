package de.regatta_hd.aquarius.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Assigns a {@link Label label} to a {@link Registration registration} including some additional data.
 */
@Entity
@Table(schema = "dbo", name = "EntryLabel")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationLabel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "EL_ID")
	@EqualsAndHashCode.Include
	private int id;

	@Column(name = "EL_IsClubMultiNOC")
	private boolean clubMultiNOC;

	@Column(name = "EL_IsCrewClubMultiNOC")
	private boolean crewClubMultiNOC;

	@Column(name = "EL_RoundFrom")
	@ToString.Include(rank = 9)
	private short roundFrom;

	@Column(name = "EL_RoundTo")
	@ToString.Include(rank = 8)
	private short roundTo;

	@ManyToOne
	@JoinColumn(name = "EL_Entry_ID_FK")
	private Registration registration;

	@ManyToOne
	@JoinColumn(name = "EL_Label_ID_FK")
	@ToString.Include(rank = 10)
	private Label label;

}