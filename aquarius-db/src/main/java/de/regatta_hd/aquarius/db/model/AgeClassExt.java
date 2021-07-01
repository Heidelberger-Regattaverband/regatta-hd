package de.regatta_hd.aquarius.db.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * An extension to the {@link AgeClass}.
 */
@Entity
@Table(schema = "dbo", name = "AgeClassExt")
//lombok
@Getter
@Setter
public class AgeClassExt {
	@Id
	@Column(name = "id")
	private int id;

	@Basic
	@Column(name = "distance")
	private int distance;

	@OneToOne
	@MapsId
	@JoinColumn(name = "id")
	private AgeClass ageClass;
}