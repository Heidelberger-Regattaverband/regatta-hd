package de.regatta_hd.aquarius.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

	@Column(name = "distance")
	private short distance;
}