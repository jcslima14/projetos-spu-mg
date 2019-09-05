public class MunicipioTipoResposta {
	private Integer municipioTipoRespostaId;
	
	private Municipio municipio;
	
	private Origem origem;

	private TipoResposta tipoResposta;
	
	public MunicipioTipoResposta(Integer municipioTipoRespostaId, Municipio municipio, Origem origem, TipoResposta tipoResposta) {
		this.municipioTipoRespostaId = municipioTipoRespostaId;
		this.municipio = municipio;
		this.origem = origem;
		this.tipoResposta = tipoResposta;
	}

	public Integer getMunicipioTipoRespostaId() {
		return municipioTipoRespostaId;
	}

	public void setMunicipioTipoRespostaId(Integer municipioTipoRespostaId) {
		this.municipioTipoRespostaId = municipioTipoRespostaId;
	}

	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
	}

	public Origem getOrigem() {
		return origem;
	}

	public void setOrigem(Origem origem) {
		this.origem = origem;
	}

	public TipoResposta getTipoResposta() {
		return tipoResposta;
	}

	public void setTipoResposta(TipoResposta tipoResposta) {
		this.tipoResposta = tipoResposta;
	}
}
