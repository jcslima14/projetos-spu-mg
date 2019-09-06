import framework.ItemComboBox;

public class TipoResposta implements ItemComboBox {
	private Integer tipoRespostaId;
	
	private String descricao;
	
	private String tipoDocumento;

	private String numeroDocumentoModelo;

	private Boolean gerarProcessoIndividual;
	
	private String unidadeAberturaProcesso;
	
	private String tipoProcesso;

	private Boolean imprimirResposta;
	
	private Integer quantidadeAssinaturas;
	
	private Origem origem;

	public TipoResposta(Integer tipoRespostaId) {
		this.tipoRespostaId = tipoRespostaId;
	}

	public TipoResposta(Integer tipoRespostaId, String descricao) {
		this.tipoRespostaId = tipoRespostaId;
		this.descricao = descricao;
	}

	public TipoResposta(Integer tipoRespostaId, String descricao, String tipoDocumento, String numeroDocumentoSEI, Boolean gerarProcessoIndividual, String unidadeAberturaProcesso, String tipoProcesso, 
			Boolean imprimirResposta, Integer quantidadeAssinaturas, Origem origem) {
		this.tipoRespostaId = tipoRespostaId;
		this.descricao = descricao;
		this.tipoDocumento = tipoDocumento;
		this.numeroDocumentoModelo = numeroDocumentoSEI;
		this.gerarProcessoIndividual = gerarProcessoIndividual;
		this.unidadeAberturaProcesso = unidadeAberturaProcesso;
		this.tipoProcesso = tipoProcesso;
		this.imprimirResposta = imprimirResposta;
		this.quantidadeAssinaturas = quantidadeAssinaturas;
		this.origem = origem;
	}

	public Integer getTipoRespostaId() {
		return tipoRespostaId;
	}

	public void setTipoRespostaId(Integer tipoRespostaId) {
		this.tipoRespostaId = tipoRespostaId;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public String getNumeroDocumentoModelo() {
		return numeroDocumentoModelo;
	}

	public void setNumeroDocumentoModelo(String numeroDocumentoSEI) {
		this.numeroDocumentoModelo = numeroDocumentoSEI;
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

	public Boolean getImprimirResposta() {
		return imprimirResposta;
	}

	public void setImprimirResposta(Boolean imprimirResposta) {
		this.imprimirResposta = imprimirResposta;
	}

	public Integer getQuantidadeAssinaturas() {
		return quantidadeAssinaturas;
	}

	public void setQuantidadeAssinaturas(Integer quantidadeAssinaturas) {
		this.quantidadeAssinaturas = quantidadeAssinaturas;
	}

	public Origem getOrigem() {
		return origem;
	}

	public void setOrigem(Origem origem) {
		this.origem = origem;
	}

	@Override
	public Integer getIntegerItemValue() {
		return getTipoRespostaId();
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