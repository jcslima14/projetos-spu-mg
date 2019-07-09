
public class AssinanteTipoDespacho {
	Integer assinanteTipoDespachoId;
	
	Assinante assinante;
	
	TipoDespacho tipoDespacho;
	
	String blocoAssinatura;

	public AssinanteTipoDespacho() {
	}

	public AssinanteTipoDespacho(Integer assinanteTipoDespachoId, Assinante assinante, TipoDespacho tipoDespacho, String blocoAssinatura) {
		this.assinanteTipoDespachoId = assinanteTipoDespachoId;
		this.assinante = assinante;
		this.tipoDespacho = tipoDespacho;
		this.blocoAssinatura = blocoAssinatura;
	}

	public Integer getAssinanteTipoDespachoId() {
		return assinanteTipoDespachoId;
	}

	public void setAssinanteTipoDespachoId(Integer assinanteTipoDespachoId) {
		this.assinanteTipoDespachoId = assinanteTipoDespachoId;
	}

	public Assinante getAssinante() {
		return assinante;
	}

	public void setAssinante(Assinante assinante) {
		this.assinante = assinante;
	}

	public TipoDespacho getTipoDespacho() {
		return tipoDespacho;
	}

	public void setTipoDespacho(TipoDespacho tipoDespacho) {
		this.tipoDespacho = tipoDespacho;
	}

	public String getBlocoAssinatura() {
		return blocoAssinatura;
	}

	public void setBlocoAssinatura(String blocoAssinatura) {
		this.blocoAssinatura = blocoAssinatura;
	}
}
