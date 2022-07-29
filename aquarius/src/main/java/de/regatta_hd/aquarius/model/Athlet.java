package de.regatta_hd.aquarius.model;

import java.util.Date;
import java.util.List;

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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This entity contains data about the athlete (rower).
 */
@Entity
@Table(schema = "dbo", name = "Athlet")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true, doNotUseGetters = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Athlet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Athlet_ID")
	@EqualsAndHashCode.Include
	private int id;

	@Column(name = "Athlet_DOB", columnDefinition = "datetime", nullable = false)
	private Date dob;

	@Column(name = "Athlet_ExternID_A", length = 11)
	private String externIDA;

	@Column(name = "Athlet_ExternID_B", length = 11)
	private String externIDB;

	@Column(name = "Athlet_ExternState")
	private Byte externState;

	@Column(name = "Athlet_ExternState_B")
	private Byte externStateB;

	@Column(name = "Athlet_FirstName", nullable = false, length = 32)
	@ToString.Include(rank = 9)
	private String firstName;

	@Column(name = "Athlet_Gender", nullable = false, length = 1)
	@ToString.Include(rank = 8)
	private String gender;

	@Column(name = "Athlet_LastName", nullable = false, length = 64)
	@ToString.Include(rank = 10)
	private String lastName;

	@Column(name = "Athlet_SoundEx", length = 5)
	private String soundEx;

	@Column(name = "Athlet_State")
	private byte state;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Athlet_Club_ID_FK")
	@ToString.Include(rank = 2)
	private Club club;

	@OneToMany(targetEntity = Crew.class, mappedBy = "athlet")
	private List<Crew> crews;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Athlet_Nation_ID_FK")
	@ToString.Include(rank = 1)
	private Nation nation;
}