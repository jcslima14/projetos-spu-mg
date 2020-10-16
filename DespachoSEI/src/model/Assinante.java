package model;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import framework.models.ItemComboBox;

@Entity
@Table(name = "assinante")
public class Assinante implements ItemComboBox {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer assinanteId;
	
	private String nome;

	private Boolean ativo;
	
	private String cargo;
	
	private String setor;
	
	private Boolean superior;

	private String numeroProcessoSEI;

	private String blocoAssinatura;

	private String pastaArquivoProcesso;

	public Assinante() {
	}
	
	public Assinante(Integer assinanteId) {
		this.assinanteId = assinanteId;
	}

	public Assinante(Integer assinanteId, String nome) {
		this.assinanteId = assinanteId;
		this.nome = nome;
	}

	public Assinante(Integer assinanteId, String nome, Boolean ativo, String cargo, String setor, Boolean superior, String numeroProcesso, String blocoAssinatura, String pastaArquivoProcesso) {
		this.assinanteId = assinanteId;
		this.nome = nome;
		this.ativo = ativo;
		this.cargo = cargo;
		this.setor = setor;
		this.superior = superior;
		this.numeroProcessoSEI = numeroProcesso;
		this.blocoAssinatura = blocoAssinatura;
		this.pastaArquivoProcesso = pastaArquivoProcesso;
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

	public String getNumeroProcessoSEI() {
		return numeroProcessoSEI;
	}

	public void setNumeroProcessoSEI(String numeroProcessoSEI) {
		this.numeroProcessoSEI = numeroProcessoSEI;
	}

	public String getBlocoAssinatura() {
		return blocoAssinatura;
	}

	public void setBlocoAssinatura(String blocoAssinatura) {
		this.blocoAssinatura = blocoAssinatura;
	}

	public String getPastaArquivoProcesso() {
		return pastaArquivoProcesso;
	}

	public void setPastaArquivoProcesso(String pastaArquivoProcesso) {
		this.pastaArquivoProcesso = pastaArquivoProcesso;
	}

	public String getSuperiorAsString() {
		if (getSuperior() == null) {
			return "";
		} else if (getSuperior()) {
			return "Sim";
		} else {
			return "Não";
		}
	}

	public String getAtivoAsString() {
		if (getAtivo() == null) {
			return "";
		} else if (getAtivo()) {
			return "Sim";
		} else {
			return "Não";
		}
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
