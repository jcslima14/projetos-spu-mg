import framework.ItemComboBox;

public class Municipio implements ItemComboBox {
	private Integer municipioId;
	
	private String nome;
	
	private Municipio municipioComarca;

	private Destino destino;

	private TipoResposta tipoResposta;
	
	public Municipio(Integer municipioId) {
		this.municipioId = municipioId;
	}

	public Municipio(Integer municipioId, String nome, Municipio municipioComarca, Destino destino, TipoResposta tipoResposta) {
		this.municipioId = municipioId;
		this.nome = nome;
		this.municipioComarca = municipioComarca;
		this.destino = destino;
		this.tipoResposta = tipoResposta;
	}

	public Integer getMunicipioId() {
		return municipioId;
	}

	public void setMunicipioId(Integer municipioId) {
		this.municipioId = municipioId;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Municipio getMunicipioComarca() {
		return municipioComarca;
	}

	public void setMunicipioComarca(Municipio municipioComarca) {
		this.municipioComarca = municipioComarca;
	}

	public Destino getDestino() {
		return destino;
	}

	public void setDestino(Destino destino) {
		this.destino = destino;
	}

	public TipoResposta getTipoResposta() {
		return tipoResposta;
	}

	public void setTipoResposta(TipoResposta tipoResposta) {
		this.tipoResposta = tipoResposta;
	}

	@Override
	public Integer getIntegerItemValue() {
		return getMunicipioId();
	}

	@Override
	public String getStringItemValue() {
		return null;
	}

	@Override
	public String getItemLabel() {
		return getNome();
	}
}
