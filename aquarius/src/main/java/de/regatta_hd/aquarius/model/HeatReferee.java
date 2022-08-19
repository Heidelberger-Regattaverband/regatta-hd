package de.regatta_hd.aquarius.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Assigns a {@link Referee referee} to a {@link Heat heat}.
 */
@Entity
@Table(schema = "dbo", name = "CompReferee")
//lombok
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class HeatReferee {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CompReferee_ID")
	@EqualsAndHashCode.Include
	private int id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CompReferee_Comp_ID_FK", nullable = false)
	private Heat heat;

	@Column(name = "CompReferee_Role", length = 1)
	private String role;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CompReferee_Referee_ID_FK", nullable = false)
	private Referee referee;
}