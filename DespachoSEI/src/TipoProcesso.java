import java.util.ArrayList;
import java.util.List;

import framework.ItemComboBox;

public class TipoProcesso implements ItemComboBox {
	
	static int ELETRONICO_ID = 1;
	static int FISICO_ID = 2;

	static TipoProcesso ELETRONICO = new TipoProcesso(ELETRONICO_ID, "Eletrônico");
	static TipoProcesso FISICO = new TipoProcesso(FISICO_ID, "Físico");

	@SuppressWarnings("serial")
	static List<TipoProcesso> TIPOS_PROCESSO = new ArrayList<TipoProcesso>() {{ 
			add(ELETRONICO); 
			add(FISICO); 
		}};

	private Integer tipoProcessoId;
	
	private String descricao;

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
