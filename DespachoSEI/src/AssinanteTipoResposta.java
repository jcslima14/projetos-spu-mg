
public class AssinanteTipoResposta {
	Integer assinanteTipoRespostaId;
	
	Assinante assinante;
	
	TipoResposta tipoResposta;
	
	String blocoAssinatura;

	public AssinanteTipoResposta() {
	}

	public AssinanteTipoResposta(Integer assinanteTipoRespostaId, Assinante assinante, TipoResposta tipoResposta, String blocoAssinatura) {
		this.assinanteTipoRespostaId = assinanteTipoRespostaId;
		this.assinante = assinante;
		this.tipoResposta = tipoResposta;
		this.blocoAssinatura = blocoAssinatura;
	}

	public Integer getAssinanteTipoRespostaId() {
		return assinanteTipoRespostaId;
	}

	public void setAssinanteTipoRespostaId(Integer assinanteTipoRespostaId) {
		this.assinanteTipoRespostaId = assinanteTipoRespostaId;
	}

	public Assinante getAssinante() {
		return assinante;
	}

	public void setAssinante(Assinante assinante) {
		this.assinante = assinante;
	}

	public TipoResposta getTipoResposta() {
		return tipoResposta;
	}

	public void setTipoResposta(TipoResposta tipoResposta) {
		this.tipoResposta = tipoResposta;
	}

	public String getBlocoAssinatura() {
		return blocoAssinatura;
	}

	public void setBlocoAssinatura(String blocoAssinatura) {
		this.blocoAssinatura = blocoAssinatura;
	}
}
