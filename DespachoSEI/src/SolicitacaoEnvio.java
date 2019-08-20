public class SolicitacaoEnvio {
	Integer solictacaoEnvioId;
	
	Solicitacao solicitacao;
	
	String dataHoraMovimentacao;
	
	String resultadoDownload;
	
	Boolean arquivosProcessados;
	
	String resultadoProcessamento;

	public SolicitacaoEnvio(Integer solicitacaoEnvioId, Solicitacao solicitacao, String dataHoraMovimentacao, String resultadoDownload, Boolean arquivosProcessados, String resultadoProcessamento) {
		this.solictacaoEnvioId = solicitacaoEnvioId;
		this.solicitacao = solicitacao;
		this.dataHoraMovimentacao = dataHoraMovimentacao;
		this.resultadoDownload = resultadoDownload;
		this.arquivosProcessados = arquivosProcessados;
		this.resultadoProcessamento = resultadoProcessamento;
	}

	public Integer getSolictacaoEnvioId() {
		return solictacaoEnvioId;
	}

	public void setSolictacaoEnvioId(Integer solictacaoEnvioId) {
		this.solictacaoEnvioId = solictacaoEnvioId;
	}

	public Solicitacao getSolicitacao() {
		return solicitacao;
	}

	public void setSolicitacao(Solicitacao solicitacao) {
		this.solicitacao = solicitacao;
	}

	public String getDataHoraMovimentacao() {
		return dataHoraMovimentacao;
	}

	public void setDataHoraMovimentacao(String dataHoraMovimentacao) {
		this.dataHoraMovimentacao = dataHoraMovimentacao;
	}

	public String getResultadoDownload() {
		return resultadoDownload;
	}

	public void setResultadoDownload(String resultadoDownload) {
		this.resultadoDownload = resultadoDownload;
	}

	public Boolean getArquivosProcessados() {
		return arquivosProcessados;
	}

	public void setArquivosProcessados(Boolean arquivosProcessados) {
		this.arquivosProcessados = arquivosProcessados;
	}

	public String getResultadoProcessamento() {
		return resultadoProcessamento;
	}

	public void setResultadoProcessamento(String resultadoProcessamento) {
		this.resultadoProcessamento = resultadoProcessamento;
	}
}
