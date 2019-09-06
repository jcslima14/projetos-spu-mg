import java.util.ArrayList;
import java.util.List;

import framework.ItemComboBox;

public class Origem implements ItemComboBox {
	
	static int SAPIENS_ID = 1;
	static int SPUNET_ID = 2;

	static Origem SAPIENS = new Origem(SAPIENS_ID, "Sapiens");
	static Origem SPUNET = new Origem(SPUNET_ID, "SPUNet");

	@SuppressWarnings("serial")
	static List<Origem> ORIGENS = new ArrayList<Origem>() {{ 
		add(SAPIENS); 
		add(SPUNET); 
		}};
	
	private Integer origemId;
	
	private String descricao;

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