import framework.ItemComboBox;

public class Assinante implements ItemComboBox {
	private Integer assinanteId;
	
	private String nome;

	private Boolean ativo;
	
	private String cargo;
	
	private String setor;
	
	private Boolean superior;

	private String numeroProcesso;

	private String blocoAssinatura;

	public Assinante(Integer assinanteId) {
		this.assinanteId = assinanteId;
	}

	public Assinante(Integer assinanteId, String nome) {
		this.assinanteId = assinanteId;
		this.nome = nome;
	}

	public Assinante(Integer assinanteId, String nome, Boolean ativo, String cargo, String setor, Boolean superior, String numeroProcesso, String blocoAssinatura) {
		this.assinanteId = assinanteId;
		this.nome = nome;
		this.ativo = ativo;
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

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
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

	@Override
	public Integer getIntegerItemValue() {
		return getAssinanteId();
	}

	@Override
	public String getStringItemValue() {
		return null;
	}

	@Override
	public String getItemLabel() {
		return getNome();
	}
}
