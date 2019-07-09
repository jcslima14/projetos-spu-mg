
public class Municipio {
	Integer municipioId;
	
	String nome;
	
	Municipio municipioComarca;

	Destino destino;

	public Municipio(Integer municipioId) {
		this.municipioId = municipioId;
	}

	public Municipio(Integer municipioId, String nome, Municipio municipioComarca, Destino destino) {
		this.municipioId = municipioId;
		this.nome = nome;
		this.municipioComarca = municipioComarca;
		this.destino = destino;
	}

	public Integer getMunicipioId() {
		return municipioId;
	}

	public void setMunicipioId(Integer municipioId) {
		this.municipioId = municipioId;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Municipio getMunicipioComarca() {
		return municipioComarca;
	}

	public void setMunicipioComarca(Municipio municipioComarca) {
		this.municipioComarca = municipioComarca;
	}

	public Destino getDestino() {
		return destino;
	}

	public void setDestino(Destino destino) {
		this.destino = destino;
	}
}
