
public class Assinante {
	Integer assinanteId;
	
	String nome;
	
	String cargo;
	
	String setor;
	
	Boolean superior;

	String numeroProcesso;

	String blocoAssinatura;

	public Assinante(Integer assinanteId) {
		this.assinanteId = assinanteId;
	}

	public Assinante(Integer assinanteId, String nome, String cargo, String setor, Boolean superior, String numeroProcesso, String blocoAssinatura) {
		this.assinanteId = assinanteId;
		this.nome = nome;
		this.cargo = cargo;
		this.setor = setor;
		this.superior = superior;
		this.numeroProcesso = numeroProcesso;
		this.blocoAssinatura = blocoAssinatura;
	}
	
	public Integer getAssinanteId() {
		return assinanteId;
	}

	public void setAssinanteId(Integer assinanteId) {
		this.assinanteId = assinanteId;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCargo() {
		return cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
	}

	public String getSetor() {
		return setor;
	}

	public void setSetor(String setor) {
		this.setor = setor;
	}

	public Boolean getSuperior() {
		return superior;
	}

	public void setSuperior(Boolean superior) {
		this.superior = superior;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getBlocoAssinatura() {
		return blocoAssinatura;
	}

	public void setBlocoAssinatura(String blocoAssinatura) {
		this.blocoAssinatura = blocoAssinatura;
	}
}
