import framework.ItemComboBox;

public class TipoImovel implements ItemComboBox {
	private Integer tipoImovelId;
	
	private String descricao;

	public TipoImovel(Integer tipoImovel, String descricao) {
		this.tipoImovelId = tipoImovel;
		this.descricao = descricao;
	}

	public TipoImovel(Integer tipoImovel) {
		this.tipoImovelId = tipoImovel;
	}

	public Integer getTipoImovelId() {
		return tipoImovelId;
	}

	public void setTipoImovelId(Integer tipoImovelId) {
		this.tipoImovelId = tipoImovelId;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Override
	public Integer getIntegerItemValue() {
		return getTipoImovelId();
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
