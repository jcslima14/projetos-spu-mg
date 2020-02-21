package model;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import framework.ItemComboBox;

@Entity
@Table(name = "tipoprocesso")
public class TipoProcesso implements ItemComboBox {
	
	public static int ELETRONICO_ID = 1;
	public static int FISICO_ID = 2;

	public static TipoProcesso ELETRONICO = new TipoProcesso(ELETRONICO_ID, "Eletrônico");
	public static TipoProcesso FISICO = new TipoProcesso(FISICO_ID, "Físico");

	@SuppressWarnings("serial")
	public static List<TipoProcesso> TIPOS_PROCESSO = new ArrayList<TipoProcesso>() {{ 
			add(ELETRONICO); 
			add(FISICO); 
		}};

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer tipoProcessoId;
	
	private String descricao;

	public TipoProcesso() {
	}
	
	public TipoProcesso(Integer tipoProcessoId) {
		this.tipoProcessoId = tipoProcessoId;
	}

	public TipoProcesso(Integer tipoProcessoId, String descricao) {
		this.tipoProcessoId = tipoProcessoId;
		this.descricao = descricao;
	}

	public Integer getTipoProcessoId() {
		return tipoProcessoId;
	}

	public void setTipoProcessoId(Integer tipoProcessoId) {
		this.tipoProcessoId = tipoProcessoId;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Override
	public Integer getIntegerItemValue() {
		return getTipoProcessoId();
	}

	@Override
	public String getStringItemValue() {
		return null;
	}

	@Override
	public String getItemLabel() {
		return getDescricao();
	}
}
