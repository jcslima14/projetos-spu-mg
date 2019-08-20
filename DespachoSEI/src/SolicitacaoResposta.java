public class SolicitacaoResposta {
	Integer solicitacaoRespostaId;
	
	Solicitacao solicitacao;
	
	TipoResposta tipoResposta;
	
	String observacao;
	
	Assinante assinante;
	
	Assinante assinanteSuperior;
	
	String numeroDocumentoSEI;
	
	String dataHoraResposta;
	
	String numeroProcessoSEI;
	
	Boolean respostaImpressa;
	
	String dataHoraImpressao;
	
	String blocoAssinatura;
	
	Boolean respostaNoBlocoAssinatura;

	public SolicitacaoResposta(Integer solicitacaoRespostaId, Solicitacao solicitacao, TipoResposta tipoResposta, String observacao, Assinante assinante, Assinante assinanteSuperior, 
			String numeroDocumentoSEI, String dataHoraResposta, String numeroProcessoSEI, Boolean respostaImpressa, String dataHoraImpressao, String blocoAssinatura, Boolean respostaNoBlocoAssinatura) {
		this.solicitacaoRespostaId = solicitacaoRespostaId;
		this.solicitacao = solicitacao;
		this.tipoResposta= tipoResposta;
		this.observacao = observacao;
		this.assinante = assinante;
		this.assinanteSuperior = assinanteSuperior;
		this.numeroDocumentoSEI = numeroDocumentoSEI;
		this.dataHoraResposta= dataHoraResposta;
		this.numeroProcessoSEI = numeroProcessoSEI;
		this.respostaImpressa = respostaImpressa;
		this.dataHoraImpressao = dataHoraImpressao;
		this.blocoAssinatura = blocoAssinatura;
		this.respostaNoBlocoAssinatura = respostaNoBlocoAssinatura;
	}

	public Integer getSolicitacaoRespostaId() {
		return solicitacaoRespostaId;
	}

	public void setSolicitacaoRespostaId(Integer solicitacaoRespostaId) {
		this.solicitacaoRespostaId = solicitacaoRespostaId;
	}

	public Solicitacao getSolicitacao() {
		return solicitacao;
	}

	public void setSolicitacao(Solicitacao solicitacao) {
		this.solicitacao = solicitacao;
	}

	public TipoResposta getTipoResposta() {
		return tipoResposta;
	}

	public void setTipoResposta(TipoResposta tipoResposta) {
		this.tipoResposta = tipoResposta;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public Assinante getAssinante() {
		return assinante;
	}

	public void setAssinante(Assinante assinante) {
		this.assinante = assinante;
	}

	public Assinante getAssinanteSuperior() {
		return assinanteSuperior;
	}

	public void setAssinanteSuperior(Assinante assinanteSuperior) {
		this.assinanteSuperior = assinanteSuperior;
	}

	public String getNumeroDocumentoSEI() {
		return numeroDocumentoSEI;
	}

	public void setNumeroDocumentoSEI(String numeroDocumentoSEI) {
		this.numeroDocumentoSEI = numeroDocumentoSEI;
	}

	public String getDataHoraResposta() {
		return dataHoraResposta;
	}

	public void setDataHoraResposta(String dataHoraResposta) {
		this.dataHoraResposta = dataHoraResposta;
	}

	public String getNumeroProcessoSEI() {
		return numeroProcessoSEI;
	}

	public void setNumeroProcessoSEI(String numeroProcessoSEI) {
		this.numeroProcessoSEI = numeroProcessoSEI;
	}

	public Boolean getRespostaImpressa() {
		return respostaImpressa;
	}

	public void setRespostaImpressa(Boolean respostaImpressa) {
		this.respostaImpressa = respostaImpressa;
	}

	public String getDataHoraImpressao() {
		return dataHoraImpressao;
	}

	public void setDataHoraImpressao(String dataHoraImpressao) {
		this.dataHoraImpressao = dataHoraImpressao;
	}

	public String getBlocoAssinatura() {
		return blocoAssinatura;
	}

	public void setBlocoAssinatura(String blocoAssinatura) {
		this.blocoAssinatura = blocoAssinatura;
	}

	public Boolean getRespostaNoBlocoAssinatura() {
		return respostaNoBlocoAssinatura;
	}

	public void setRespostaNoBlocoAssinatura(Boolean respostaNoBlocoAssinatura) {
		this.respostaNoBlocoAssinatura = respostaNoBlocoAssinatura;
	}
}
