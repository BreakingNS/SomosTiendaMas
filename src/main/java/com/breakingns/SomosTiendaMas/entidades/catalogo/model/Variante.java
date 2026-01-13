package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// relaciones a entidades dependientes: precios, inventario, imágenes
//import com.breakingns.SomosTiendaMas.entidades.catalogo.model.PrecioVariante;
//import com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioVariante;
//import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ImagenVariante;

@Entity
@Table(name = "variante",
	indexes = {
		@Index(name = "ux_variante_sku", columnList = "sku", unique = true),
		@Index(name = "ix_variante_producto", columnList = "producto_id")
	},
	uniqueConstraints = {
		@UniqueConstraint(name = "ux_variante_producto_attributes_hash", columnNames = {"producto_id", "attributes_hash"})
	}
)
@Getter
@Setter
public class Variante extends BaseEntidadAuditada {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "producto_id", nullable = false)
	private Producto producto;

	@Column(name = "sku", length = 120, nullable = false, unique = true)
	private String sku;

	@Column(name = "attributes_json", columnDefinition = "TEXT")
	private String attributesJson;

	@Column(name = "attributes_hash", length = 64)
	private String attributesHash;

	@Column(name = "es_default", nullable = false)
	private boolean esDefault = false;

	@Column(name = "activo", nullable = false)
	private boolean activo = true;

	@Version
	private Long version;

	// -----------------------------
	// Relaciones dependientes
	// -----------------------------
	@OneToMany(mappedBy = "variante", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PrecioVariante> precios = new ArrayList<>();

	@OneToOne(mappedBy = "variante", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private VarianteFisico fisico;

	@OneToOne(mappedBy = "variante", cascade = CascadeType.ALL, orphanRemoval = true)
	private InventarioVariante inventario;

	@OneToMany(mappedBy = "variante", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ImagenVariante> imagenes = new ArrayList<>();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Variante)) return false;
		Variante variante = (Variante) o;
		return Objects.equals(getId(), variante.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}

}
