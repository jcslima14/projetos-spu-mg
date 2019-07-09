
public class TipoDespacho {
	Integer tipoDespachoId;
	
	String descricao;
	
	String numeroDocumentoSEI;

	Boolean gerarProcessoIndividual;
	
	String unidadeAberturaProcesso;
	
	String tipoProcesso;
	
	public TipoDespacho(Integer tipoDespachoId) {
		this.tipoDespachoId = tipoDespachoId;
	}

	public TipoDespacho(Integer tipoDespachoId, String descricao, String numeroDocumentoSEI, Boolean gerarProcessoIndividual, String unidadeAberturaProcesso, String tipoProcesso) {
		this.tipoDespachoId = tipoDespachoId;
		this.descricao = descricao;
		this.numeroDocumentoSEI = numeroDocumentoSEI;
		this.gerarProcessoIndividual = gerarProcessoIndividual;
		this.unidadeAberturaProcesso = unidadeAberturaProcesso;
		this.tipoProcesso = tipoProcesso;
	}

	public Integer getTipoDespachoId() {
		return tipoDespachoId;
	}

	public void setTipoDespachoId(Integer tipoDespachoId) {
		this.tipoDespachoId = tipoDespachoId;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getNumeroDocumentoSEI() {
		return numeroDocumentoSEI;
	}

	public void setNumeroDocumentoSEI(String numeroDocumentoSEI) {
		this.numeroDocumentoSEI = numeroDocumentoSEI;
	}

	public Boolean getGerarProcessoIndividual() {
		return gerarProcessoIndividual;
	}

	public void setGerarProcessoIndividual(Boolean gerarProcessoIndividual) {
		this.gerarProcessoIndividual = gerarProcessoIndividual;
	}

	public String getUnidadeAberturaProcesso() {
		return unidadeAberturaProcesso;
	}

	public void setUnidadeAberturaProcesso(String unidadeAberturaProcesso) {
		this.unidadeAberturaProcesso = unidadeAberturaProcesso;
	}

	public String getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(String tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}
}
