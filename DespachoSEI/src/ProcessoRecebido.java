public class ProcessoRecebido {
	Integer processoRecebidoId;
	
	String numeroUnico;
	
	String dataHoraMovimentacao;
	
	Municipio municipio;
	
	String resultadoDownload;
	
	Boolean arquivosProcessados;
	
	String resultadoProcessamento;

	public ProcessoRecebido(Integer processoRecebidoId, String numeroUnico, String dataHoraMovimentacao, Municipio municipio, String resultadoDownload, Boolean arquivosProcessados, String resultadoProcessamento) {
		this.processoRecebidoId = processoRecebidoId;
		this.numeroUnico = numeroUnico;
		this.dataHoraMovimentacao = dataHoraMovimentacao;
		this.municipio = municipio;
		this.resultadoDownload = resultadoDownload;
		this.arquivosProcessados = arquivosProcessados;
		this.resultadoProcessamento = resultadoProcessamento;
	}

	public Integer getProcessoRecebidoId() {
		return processoRecebidoId;
	}

	public void setProcessoRecebidoId(Integer processoRecebidoId) {
		this.processoRecebidoId = processoRecebidoId;
	}

	public String getNumeroUnico() {
		return numeroUnico;
	}

	public void setNumeroUnico(String numeroUnico) {
		this.numeroUnico = numeroUnico;
	}

	public String getDataHoraMovimentacao() {
		return dataHoraMovimentacao;
	}

	public void setDataHoraMovimentacao(String dataHoraMovimentacao) {
		this.dataHoraMovimentacao = dataHoraMovimentacao;
	}

	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
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
