import framework.ItemComboBox;

public class Destino implements ItemComboBox {
	private Integer destinoId;
	
	private String artigo;
	
	private String descricao;

	private String abreviacao;

	private Boolean usarCartorio;
	
	public Destino(Integer destinoId) {
		this.destinoId = destinoId;
	}
	
	public Destino(Integer destinoId, String artigo, String descricao, String abreviacao, Boolean usarCartorio) {
		this.destinoId = destinoId;
		this.artigo = artigo;
		this.descricao = descricao;
		this.abreviacao = abreviacao;
		this.usarCartorio = usarCartorio;
	}
	
	public Integer getDestinoId() {
		return destinoId;
	}

	public void setDestinoId(Integer destinoId) {
		this.destinoId = destinoId;
	}

	public String getArtigo() {
		return artigo;
	}

	public void setArtigo(String artigo) {
		this.artigo = artigo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getAbreviacao() {
		return abreviacao;
	}

	public void setAbreviacao(String abreviacao) {
		this.abreviacao = abreviacao;
	}

	@Override
	public Integer getIntegerItemValue() {
		return getDestinoId();
	}

	@Override
	public String getStringItemValue() {
		return null;
	}

	@Override
	public String getItemLabel() {
		return getDescricao();
	}

	public Boolean getUsarCartorio() {
		return usarCartorio;
	}

	public void setUsarCartorio(Boolean usarCartorio) {
		this.usarCartorio = usarCartorio;
	}
}
