public class Despacho {
	Integer despachoId;
	
	String dataDespacho;
	
	TipoProcesso tipoProcesso;
	
	String numeroProcesso;
	
	String autor;
	
	String comarca;
	
	TipoImovel tipoImovel;
	
	String endereco;
	
	String municipio;
	
	String coordenada;
	
	String area;
	
	TipoDespacho tipoDespacho;
	
	Assinante assinante;
	
	Destino destino;
	
	String observacao;
	
	String numeroDocumentoSEI;
	
	String dataHoraDespacho;
	
	String numeroProcessoSEI;

	Boolean arquivosAnexados;
	
	Boolean despachoImpresso;
	
	String dataHoraImpressao;
	
	String blocoAssinatura;
	
	Boolean despachoNoBlocoAssinatura;

	public Despacho(Integer despachoId, String dataDespacho, TipoProcesso tipoProcesso, String numeroProcesso, String autor, String comarca, TipoImovel tipoImovel, 
			String endereco, String municipio, String coordenada, String area, TipoDespacho tipoDespacho, Assinante assinante, Destino destino, String observacao,
			String numeroDocumentoSEI, String dataHoraDespacho, String numeroProcessoSEI, Boolean arquivosAnexados, Boolean despachoImpresso, String dataHoraImpressao,
			String blocoAssinatura, Boolean despachoNoBlocoAssinatura) {
		this.despachoId = despachoId;
		this.dataDespacho = dataDespacho;
		this.tipoProcesso = tipoProcesso;
		this.numeroProcesso = numeroProcesso;
		this.autor = autor;
		this.comarca = comarca;
		this.tipoImovel = tipoImovel;
		this.endereco = endereco;
		this.municipio = municipio;
		this.coordenada = coordenada;
		this.area = area;
		this.tipoDespacho = tipoDespacho;
		this.assinante = assinante;
		this.destino = destino;
		this.observacao = observacao;
		this.numeroDocumentoSEI = numeroDocumentoSEI;
		this.dataHoraDespacho = dataHoraDespacho;
		this.numeroProcessoSEI = numeroProcessoSEI;
		this.arquivosAnexados = arquivosAnexados;
		this.despachoImpresso = despachoImpresso;
		this.dataHoraImpressao = dataHoraImpressao;
		this.blocoAssinatura = blocoAssinatura;
		this.despachoNoBlocoAssinatura = despachoNoBlocoAssinatura;
	}

	public Integer getDespachoId() {
		return despachoId;
	}

	public void setDespachoId(Integer despachoId) {
		this.despachoId = despachoId;
	}

	public String getDataDespacho() {
		return dataDespacho;
	}

	public void setDataDespacho(String dataDespacho) {
		this.dataDespacho = dataDespacho;
	}

	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getAutor() {
		return autor;
	}

	public void setAutor(String autor) {
		this.autor = autor;
	}

	public String getComarca() {
		return comarca;
	}

	public void setComarca(String comarca) {
		this.comarca = comarca;
	}

	public TipoImovel getTipoImovel() {
		return tipoImovel;
	}

	public void setTipoImovel(TipoImovel tipoImovel) {
		this.tipoImovel = tipoImovel;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getMunicipio() {
		return municipio;
	}

	public void setMunicipio(String municipio) {
		this.municipio = municipio;
	}

	public String getCoordenada() {
		return coordenada;
	}

	public void setCoordenada(String coordenada) {
		this.coordenada = coordenada;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public TipoDespacho getTipoDespacho() {
		return tipoDespacho;
	}

	public void setTipoDespacho(TipoDespacho tipoDespacho) {
		this.tipoDespacho = tipoDespacho;
	}

	public Assinante getAssinante() {
		return assinante;
	}

	public void setAssinante(Assinante assinante) {
		this.assinante = assinante;
	}

	public Destino getDestino() {
		return destino;
	}

	public void setDestino(Destino destino) {
		this.destino = destino;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public String getNumeroDocumentoSEI() {
		return numeroDocumentoSEI;
	}

	public void setNumeroDocumentoSEI(String numeroDocumentoSEI) {
		this.numeroDocumentoSEI = numeroDocumentoSEI;
	}

	public String getDataHoraDespacho() {
		return dataHoraDespacho;
	}

	public void setDataHoraDespacho(String dataHoraDespacho) {
		this.dataHoraDespacho = dataHoraDespacho;
	}

	public String getNumeroProcessoSEI() {
		return numeroProcessoSEI;
	}

	public void setNumeroProcessoSEI(String numeroProcessoSEI) {
		this.numeroProcessoSEI = numeroProcessoSEI;
	}

	public Boolean getArquivosAnexados() {
		return arquivosAnexados;
	}

	public void setArquivosAnexados(Boolean arquivosAnexados) {
		this.arquivosAnexados = arquivosAnexados;
	}

	public Boolean getDespachoImpresso() {
		return despachoImpresso;
	}

	public void setDespachoImpresso(Boolean despachoImpresso) {
		this.despachoImpresso = despachoImpresso;
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

	public Boolean getDespachoNoBlocoAssinatura() {
		return despachoNoBlocoAssinatura;
	}

	public void setDespachoNoBlocoAssinatura(Boolean despachoNoBlocoAssinatura) {
		this.despachoNoBlocoAssinatura = despachoNoBlocoAssinatura;
	}
}
