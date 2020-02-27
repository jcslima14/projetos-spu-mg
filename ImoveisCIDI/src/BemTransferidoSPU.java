import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "bemtransferidospu")
public class BemTransferidoSPU {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer bemTransferidoSPUId;
	
	private String ur;
	
	private String nbp;
	
	private String parcela;
	
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
	
	private String situacaoBp;
	
	private String bpTerreno;
	
	private String parcBpTerreno;
	
	private String origem;
	
	private String nProcesso;
	
	private String matriculaRgi;
	
	private String checkList;
	
	private String checkAno;
	
	private String termoTransf;
	
	private String termoAno;
	
	private String termoDataSit;
	
	private String termoOficioInv;
	
	private String termoOficioNum;
	
	private String situacaoSPU;
	
	private String destinProv;
	
	private String interessePub;
	
	private String registroCri;
	
	private String valorHistArtCult;
	
	private String avalOrgData;
	
	private String incorporado;
	
	private String rip;

	public Integer getBemTransferidoSPUId() {
		return bemTransferidoSPUId;
	}

	public void setBemTransferidoSPUId(Integer bemTransferidoSPUId) {
		this.bemTransferidoSPUId = bemTransferidoSPUId;
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

	public String getSituacaoBp() {
		return situacaoBp;
	}

	public void setSituacaoBp(String situacaoBp) {
		this.situacaoBp = situacaoBp;
	}

	public String getBpTerreno() {
		return bpTerreno;
	}

	public void setBpTerreno(String bpTerreno) {
		this.bpTerreno = bpTerreno;
	}

	public String getParcBpTerreno() {
		return parcBpTerreno;
	}

	public void setParcBpTerreno(String parcBpTerreno) {
		this.parcBpTerreno = parcBpTerreno;
	}

	public String getOrigem() {
		return origem;
	}

	public void setOrigem(String origem) {
		this.origem = origem;
	}

	public String getNProcesso() {
		return nProcesso;
	}

	public void setNProcesso(String nProcesso) {
		this.nProcesso = nProcesso;
	}

	public String getMatriculaRgi() {
		return matriculaRgi;
	}

	public void setMatriculaRgi(String matriculaRgi) {
		this.matriculaRgi = matriculaRgi;
	}

	public String getCheckList() {
		return checkList;
	}

	public void setCheckList(String checkList) {
		this.checkList = checkList;
	}

	public String getCheckAno() {
		return checkAno;
	}

	public void setCheckAno(String checkAno) {
		this.checkAno = checkAno;
	}

	public String getTermoTransf() {
		return termoTransf;
	}

	public void setTermoTransf(String termoTransf) {
		this.termoTransf = termoTransf;
	}

	public String getTermoAno() {
		return termoAno;
	}

	public void setTermoAno(String termoAno) {
		this.termoAno = termoAno;
	}

	public String getTermoDataSit() {
		return termoDataSit;
	}

	public void setTermoDataSit(String termoDataSit) {
		this.termoDataSit = termoDataSit;
	}

	public String getTermoOficioInv() {
		return termoOficioInv;
	}

	public void setTermoOficioInv(String termoOficioInv) {
		this.termoOficioInv = termoOficioInv;
	}

	public String getTermoOficioNum() {
		return termoOficioNum;
	}

	public void setTermoOficioNum(String termoOficioNum) {
		this.termoOficioNum = termoOficioNum;
	}

	public String getSituacaoSPU() {
		return situacaoSPU;
	}

	public void setSituacaoSPU(String situacaoSPU) {
		this.situacaoSPU = situacaoSPU;
	}

	public String getDestinProv() {
		return destinProv;
	}

	public void setDestinProv(String destinProv) {
		this.destinProv = destinProv;
	}

	public String getInteressePub() {
		return interessePub;
	}

	public void setInteressePub(String interessePub) {
		this.interessePub = interessePub;
	}

	public String getRegistroCri() {
		return registroCri;
	}

	public void setRegistroCri(String registroCri) {
		this.registroCri = registroCri;
	}

	public String getValorHistArtCult() {
		return valorHistArtCult;
	}

	public void setValorHistArtCult(String valorHistArtCult) {
		this.valorHistArtCult = valorHistArtCult;
	}

	public String getAvalOrgData() {
		return avalOrgData;
	}

	public void setAvalOrgData(String avalOrgData) {
		this.avalOrgData = avalOrgData;
	}

	public String getIncorporado() {
		return incorporado;
	}

	public void setIncorporado(String incorporado) {
		this.incorporado = incorporado;
	}

	public String getRip() {
		return rip;
	}

	public void setRip(String rip) {
		this.rip = rip;
	}
}
