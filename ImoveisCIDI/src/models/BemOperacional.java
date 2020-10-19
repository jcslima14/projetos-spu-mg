package models;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "bemoperacional")
public class BemOperacional {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer bemOperacionalId;
	
	private String ur;
	
	private String nbp;
	
	private String parcela;
	
	private String grupo;
	
	private String conta;
	
	private String descricao;
	
	private String codigoTrecho;
	
	private String trechoInicio;
	
	private String trechoFim;
	
	private String logradouro;
	
	private String complemento;
	
	private String municipio;
	
	private String cep;
	
	private String uf;
	
	private String area;
	
	private String situacao;
	
	private String bpTerreno;
	
	private String parcTerreno;

	public Integer getBemOperacionalId() {
		return bemOperacionalId;
	}

	public void setBemOperacionalId(Integer bemOperacionalId) {
		this.bemOperacionalId = bemOperacionalId;
	}

	public String getUr() {
		return ur;
	}

	public void setUr(String ur) {
		this.ur = ur;
	}

	public String getNbp() {
		return nbp;
	}

	public void setNbp(String nbp) {
		this.nbp = nbp;
	}

	public String getParcela() {
		return parcela;
	}

	public void setParcela(String parcela) {
		this.parcela = parcela;
	}

	public String getGrupo() {
		return grupo;
	}

	public void setGrupo(String grupo) {
		this.grupo = grupo;
	}

	public String getConta() {
		return conta;
	}

	public void setConta(String conta) {
		this.conta = conta;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getCodigoTrecho() {
		return codigoTrecho;
	}

	public void setCodigoTrecho(String codigoTrecho) {
		this.codigoTrecho = codigoTrecho;
	}

	public String getTrechoInicio() {
		return trechoInicio;
	}

	public void setTrechoInicio(String trechoInicio) {
		this.trechoInicio = trechoInicio;
	}

	public String getTrechoFim() {
		return trechoFim;
	}

	public void setTrechoFim(String trechoFim) {
		this.trechoFim = trechoFim;
	}

	public String getLogradouro() {
		return logradouro;
	}

	public void setLogradouro(String logradouro) {
		this.logradouro = logradouro;
	}

	public String getComplemento() {
		return complemento;
	}

	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}

	public String getMunicipio() {
		return municipio;
	}

	public void setMunicipio(String municipio) {
		this.municipio = municipio;
	}

	public String getCep() {
		return cep;
	}

	public void setCep(String cep) {
		this.cep = cep;
	}

	public String getUf() {
		return uf;
	}

	public void setUf(String uf) {
		this.uf = uf;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getSituacao() {
		return situacao;
	}

	public void setSituacao(String situacao) {
		this.situacao = situacao;
	}

	public String getBpTerreno() {
		return bpTerreno;
	}

	public void setBpTerreno(String bpTerreno) {
		this.bpTerreno = bpTerreno;
	}

	public String getParcTerreno() {
		return parcTerreno;
	}

	public void setParcTerreno(String parcTerreno) {
		this.parcTerreno = parcTerreno;
	}
}
