
public class Destino {
	Integer destinoId;
	
	String artigo;
	
	String descricao;

	Boolean usarComarca;
	
	public Destino(Integer destinoId) {
		this.destinoId = destinoId;
	}
	
	public Destino(Integer destinoId, String artigo, String descricao, Boolean usarComarca) {
		this.destinoId = destinoId;
		this.artigo = artigo;
		this.descricao = descricao;
		this.usarComarca = usarComarca;
	}
	
	public Integer getDestinoId() {
		return destinoId;
	}

	public void setDestinoId(Integer destinoId) {
		this.destinoId = destinoId;
	}

	public String getArtigo() {
		return artigo;
	}

	public void setArtigo(String artigo) {
		this.artigo = artigo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Boolean getUsarComarca() {
		return usarComarca;
	}

	public void setUsarComarca(Boolean usarComarca) {
		this.usarComarca = usarComarca;
	}
}
