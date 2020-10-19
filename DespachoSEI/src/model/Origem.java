package model;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import framework.models.ItemComboBox;

@Entity
@Table(name = "origem")
public class Origem implements ItemComboBox {
	
	public static int SAPIENS_ID = 1;
	public static int SPUNET_ID = 2;

	public static Origem SAPIENS = new Origem(SAPIENS_ID, "Sapiens");
	public static Origem SPUNET = new Origem(SPUNET_ID, "SPUNet");

	@SuppressWarnings("serial")
	public static List<Origem> ORIGENS = new ArrayList<Origem>() {{ 
		add(SAPIENS); 
		add(SPUNET); 
		}};
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer origemId;
	
	private String descricao;

	public Origem() {
	}
	
	public Origem(Integer origemId, String descricao) {
		this.origemId = origemId;
		this.descricao = descricao;
	}

	public Origem(Integer origemId) {
		this.origemId = origemId;
	}

	public Integer getOrigemId() {
		return origemId;
	}

	public void setOrigemId(Integer origemId) {
		this.origemId = origemId;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Override
	public Integer getIntegerItemValue() {
		return getOrigemId();
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
