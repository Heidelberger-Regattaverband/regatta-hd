package de.regatta_hd.aquarius.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An extension to the {@link Race}.
 */
@Entity
@Table(schema = "dbo", name = "OfferExt")
//lombok
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaceExt {

	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "isSet")
	private boolean set;
}