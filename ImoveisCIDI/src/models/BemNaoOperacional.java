package models;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "bemnaooperacional")
public class BemNaoOperacional {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer bemNaoOperacionalId;
	
	private String ur;
	
	private String nbp;
	
	private String parcela;
	
	private String edital;
	
	private String contratoSarp;
	
	private String conta;
	
	private String descricao;
	
	private String codigoTrecho;
	
	private String trechoInicio;
	
	private String trechoFim;
	
	private String codigoCls;
	
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
	
	private String interessado;
	
	private String nomeCnbp;
	
	private String vlContabil;
	
	private String averbacao;
	
	private String escrDef;
	
	private String registroGeral;
	
	private String planta;
	
	private String memorialDescritivo;
	
	private String nProcesso;
	
	private String matriculaRgi;
	
	private String fundoCont;
	
	private String checkList;
	
	private String checkAno;
	
	private String checkSituacao;
	
	private String termoTransf;
	
	private String termoAno;
	
	private String termoSituacao;
	
	private String termoDataSit;
	
	private String notaTecnica;
	
	private String termoOficioInv;
	
	private String termoOficioNum;
	
	private String observacao;

	public Integer getBemNaoOperacionalId() {
		return bemNaoOperacionalId;
	}

	public void setBemNaoOperacionalId(Integer bemNaoOperacionalId) {
		this.bemNaoOperacionalId = bemNaoOperacionalId;
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

	public String getEdital() {
		return edital;
	}

	public void setEdital(String edital) {
		this.edital = edital;
	}

	public String getContratoSarp() {
		return contratoSarp;
	}

	public void setContratoSarp(String contratoSarp) {
		this.contratoSarp = contratoSarp;
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

	public String getCodigoCls() {
		return codigoCls;
	}

	public void setCodigoCls(String codigoCls) {
		this.codigoCls = codigoCls;
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

	public String getInteressado() {
		return interessado;
	}

	public void setInteressado(String interessado) {
		this.interessado = interessado;
	}

	public String getNomeCnbp() {
		return nomeCnbp;
	}

	public void setNomeCnbp(String nomeCnbp) {
		this.nomeCnbp = nomeCnbp;
	}

	public String getVlContabil() {
		return vlContabil;
	}

	public void setVlContabil(String vlContabil) {
		this.vlContabil = vlContabil;
	}

	public String getAverbacao() {
		return averbacao;
	}

	public void setAverbacao(String averbacao) {
		this.averbacao = averbacao;
	}

	public String getEscrDef() {
		return escrDef;
	}

	public void setEscrDef(String escrDef) {
		this.escrDef = escrDef;
	}

	public String getRegistroGeral() {
		return registroGeral;
	}

	public void setRegistroGeral(String registroGeral) {
		this.registroGeral = registroGeral;
	}

	public String getPlanta() {
		return planta;
	}

	public void setPlanta(String planta) {
		this.planta = planta;
	}

	public String getMemorialDescritivo() {
		return memorialDescritivo;
	}

	public void setMemorialDescritivo(String memorialDescritivo) {
		this.memorialDescritivo = memorialDescritivo;
	}

	public String getnProcesso() {
		return nProcesso;
	}

	public void setnProcesso(String nProcesso) {
		this.nProcesso = nProcesso;
	}

	public String getMatriculaRgi() {
		return matriculaRgi;
	}

	public void setMatriculaRgi(String matriculaRgi) {
		this.matriculaRgi = matriculaRgi;
	}

	public String getFundoCont() {
		return fundoCont;
	}

	public void setFundoCont(String fundoCont) {
		this.fundoCont = fundoCont;
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

	public String getCheckSituacao() {
		return checkSituacao;
	}

	public void setCheckSituacao(String checkSituacao) {
		this.checkSituacao = checkSituacao;
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

	public String getTermoSituacao() {
		return termoSituacao;
	}

	public void setTermoSituacao(String termoSituacao) {
		this.termoSituacao = termoSituacao;
	}

	public String getTermoDataSit() {
		return termoDataSit;
	}

	public void setTermoDataSit(String termoDataSit) {
		this.termoDataSit = termoDataSit;
	}

	public String getNotaTecnica() {
		return notaTecnica;
	}

	public void setNotaTecnica(String notaTecnica) {
		this.notaTecnica = notaTecnica;
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

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}
}
