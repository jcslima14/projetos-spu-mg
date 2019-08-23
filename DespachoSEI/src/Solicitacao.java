public class Solicitacao {
	private Integer solicitacaoId;
	
	private Origem origem;
	
	private TipoProcesso tipoProcesso;
	
	private String numeroProcesso;
	
	private String autor;
	
	private Municipio municipio;
	
	private Destino destino;
	
	private String cartorio;
	
	private TipoImovel tipoImovel;
	
	private String endereco;
	
	private String coordenada;
	
	private String area;
	
	private String numeroProcessoSEI;

	private Boolean arquivosAnexados;

	public Solicitacao(Origem origem, TipoProcesso tipoProcesso, String numeroProcesso, String autor) {
		this.origem = origem;
		this.tipoProcesso = tipoProcesso;
		this.numeroProcesso = numeroProcesso;
		this.autor = autor;
	}

	public Solicitacao(Integer solicitacaoId, Origem origem, TipoProcesso tipoProcesso, String numeroProcesso, String autor, Municipio municipio, Destino destino, String cartorio, TipoImovel tipoImovel, 
			String endereco, String coordenada, String area, String numeroProcessoSEI, Boolean arquivosAnexados) {
		this.solicitacaoId = solicitacaoId;
		this.origem = origem;
		this.tipoProcesso = tipoProcesso;
		this.numeroProcesso = numeroProcesso;
		this.autor = autor;
		this.municipio = municipio;
		this.destino = destino;
		this.cartorio = cartorio;
		this.tipoImovel = tipoImovel;
		this.endereco = endereco;
		this.coordenada = coordenada;
		this.area = area;
		this.numeroProcessoSEI = numeroProcessoSEI;
		this.arquivosAnexados = arquivosAnexados;
	}

	public Integer getSolicitacaoId() {
		return solicitacaoId;
	}

	public void setSolicitacaoId(Integer solicitacaoId) {
		this.solicitacaoId = solicitacaoId;
	}

	public Origem getOrigem() {
		return origem;
	}

	public void setOrigem(Origem origem) {
		this.origem = origem;
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

	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
	}

	public Destino getDestino() {
		return destino;
	}

	public void setDestino(Destino destino) {
		this.destino = destino;
	}

	public String getCartorio() {
		return cartorio;
	}

	public void setCartorio(String cartorio) {
		this.cartorio = cartorio;
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
}
