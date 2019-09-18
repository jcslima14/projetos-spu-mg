import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "geoinformacao")
public class Geoinformacao {
	@Transient
	@SuppressWarnings("serial")
	private List<String> ajusteDeEscala = new ArrayList<String>() {{ 
		add("0"); 
		add("1:50"); 
		add("1:75"); 
		add("1:100"); 
		add("1:125"); 
		add("1:150"); 
		add("1:200"); 
		add("1:250"); 
		add("1:300"); 
		add("1:350"); 
		add("1:400"); 
		add("1:700"); 
		add("1:750"); 
		add("1:1250"); 
		add("1:1500"); 
		add("1:2100"); 
		add("1:2225"); 
		add("1:3000"); 
		add("1:4000"); 
	}};
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer geoinformacaoId;
	
	private Boolean cadastrado;
	
	private String identFormatoProdutoCDG;
	
	private String identProdutoCDG;
	
	private String identTituloProduto;
	
	private String identDataCriacao;
	
	private String identDataDigitalizacao;
	
	private String identResumo;
	
	private String identStatus;
	
	private String identInstituicao;
	
	private String identFuncao;
	
	private String sisrefDatum;
	
	private String sisrefProjecao;
	
	private String sisrefObservacao;
	
	private String identcdgTipoReprEspacial;

	private String identcdgEscala;

	private String identcdgIdioma;

	private String identcdgCategoria;

	private String identcdgUF;

	private String identcdgMunicipio;

	private String identcdgDatum;

	private String qualidadeNivel;

	private String qualidadeLinhagem;

	private String distribuicaoFormato;

	private String distribuicaoInstituicao;

	private String distribuicaoFuncao;

	private String metadadoIdioma;

	private String metadadoInstituicao;

	private String metadadoFuncao;

	private String infadicTipoArticulacao;

	private String infadicCamadaInf;

	public Integer getGeoinformacaoId() {
		return geoinformacaoId;
	}

	public void setGeoinformacaoId(Integer geoinformacaoId) {
		this.geoinformacaoId = geoinformacaoId;
	}

	public Boolean getCadastrado() {
		return cadastrado;
	}

	public void setCadastrado(Boolean cadastrado) {
		this.cadastrado = cadastrado;
	}

	public String getIdentFormatoProdutoCDG() {
		return identFormatoProdutoCDG;
	}

	public void setIdentFormatoProdutoCDG(String identFormatoProdutoCDG) {
		this.identFormatoProdutoCDG = identFormatoProdutoCDG;
	}

	public String getIdentProdutoCDG() {
		return identProdutoCDG;
	}

	public void setIdentProdutoCDG(String identProdutoCDG) {
		this.identProdutoCDG = identProdutoCDG;
	}

	public String getIdentTituloProduto() {
		return identTituloProduto;
	}

	public void setIdentTituloProduto(String identTituloProduto) {
		this.identTituloProduto = identTituloProduto;
	}

	public String getIdentDataCriacao() {
		return identDataCriacao;
	}

	public void setIdentDataCriacao(String identDataCriacao) {
		this.identDataCriacao = identDataCriacao;
	}

	public String getIdentDataDigitalizacao() {
		return identDataDigitalizacao;
	}

	public void setIdentDataDigitalizacao(String identDataDigitalizacao) {
		this.identDataDigitalizacao = identDataDigitalizacao;
	}

	public String getIdentResumo() {
		return identResumo;
	}

	public void setIdentResumo(String identResumo) {
		this.identResumo = identResumo;
	}

	public String getIdentStatus() {
		return identStatus;
	}

	public void setIdentStatus(String identStatus) {
		this.identStatus = identStatus;
	}

	public String getIdentInstituicao() {
		return identInstituicao;
	}

	public void setIdentInstituicao(String identInstituicao) {
		this.identInstituicao = identInstituicao;
	}

	public String getIdentFuncao() {
		return identFuncao;
	}

	public void setIdentFuncao(String identFuncao) {
		this.identFuncao = identFuncao;
	}

	public String getSisrefDatum() {
		return sisrefDatum;
	}

	public void setSisrefDatum(String sisrefDatum) {
		this.sisrefDatum = sisrefDatum;
	}

	public String getSisrefProjecao() {
		return sisrefProjecao;
	}

	public void setSisrefProjecao(String sisrefProjecao) {
		this.sisrefProjecao = sisrefProjecao;
	}

	public String getSisrefObservacao() {
		return sisrefObservacao;
	}

	public void setSisrefObservacao(String sisrefObservacao) {
		this.sisrefObservacao = sisrefObservacao;
	}

	public String getIdentcdgTipoReprEspacial() {
		return identcdgTipoReprEspacial;
	}

	public void setIdentcdgTipoReprEspacial(String identcdgTipoReprEspacial) {
		this.identcdgTipoReprEspacial = identcdgTipoReprEspacial;
	}

	public String getIdentcdgEscala() {
		return identcdgEscala;
	}

	public void setIdentcdgEscala(String identcdgEscala) {
		this.identcdgEscala = identcdgEscala;
	}

	public String getIdentcdgIdioma() {
		return identcdgIdioma;
	}

	public void setIdentcdgIdioma(String identcdgIdioma) {
		this.identcdgIdioma = identcdgIdioma;
	}

	public String getIdentcdgCategoria() {
		return identcdgCategoria;
	}

	public void setIdentcdgCategoria(String identcdgCategoria) {
		this.identcdgCategoria = identcdgCategoria;
	}

	public String getIdentcdgUF() {
		return identcdgUF;
	}

	public void setIdentcdgUF(String identcdgUF) {
		this.identcdgUF = identcdgUF;
	}

	public String getIdentcdgMunicipio() {
		return identcdgMunicipio;
	}

	public void setIdentcdgMunicipio(String identcdgMunicipio) {
		this.identcdgMunicipio = identcdgMunicipio;
	}

	public String getIdentcdgDatum() {
		return identcdgDatum;
	}

	public void setIdentcdgDatum(String identcdgDatum) {
		this.identcdgDatum = identcdgDatum;
	}

	public String getQualidadeNivel() {
		return qualidadeNivel;
	}

	public void setQualidadeNivel(String qualidadeNivel) {
		this.qualidadeNivel = qualidadeNivel;
	}

	public String getQualidadeLinhagem() {
		return qualidadeLinhagem;
	}

	public void setQualidadeLinhagem(String qualidadeLinhagem) {
		this.qualidadeLinhagem = qualidadeLinhagem;
	}

	public String getDistribuicaoFormato() {
		return distribuicaoFormato;
	}

	public void setDistribuicaoFormato(String distribuicaoFormato) {
		this.distribuicaoFormato = distribuicaoFormato;
	}

	public String getDistribuicaoInstituicao() {
		return distribuicaoInstituicao;
	}

	public void setDistribuicaoInstituicao(String distribuicaoInstituicao) {
		this.distribuicaoInstituicao = distribuicaoInstituicao;
	}

	public String getDistribuicaoFuncao() {
		return distribuicaoFuncao;
	}

	public void setDistribuicaoFuncao(String distribuicaoFuncao) {
		this.distribuicaoFuncao = distribuicaoFuncao;
	}

	public String getMetadadoIdioma() {
		return metadadoIdioma;
	}

	public void setMetadadoIdioma(String metadadoIdioma) {
		this.metadadoIdioma = metadadoIdioma;
	}

	public String getMetadadoInstituicao() {
		return metadadoInstituicao;
	}

	public void setMetadadoInstituicao(String metadadoInstituicao) {
		this.metadadoInstituicao = metadadoInstituicao;
	}

	public String getMetadadoFuncao() {
		return metadadoFuncao;
	}

	public void setMetadadoFuncao(String metadadoFuncao) {
		this.metadadoFuncao = metadadoFuncao;
	}

	public String getInfadicTipoArticulacao() {
		return infadicTipoArticulacao;
	}

	public void setInfadicTipoArticulacao(String infadicTipoArticulacao) {
		this.infadicTipoArticulacao = infadicTipoArticulacao;
	}

	public String getInfadicCamadaInf() {
		return infadicCamadaInf;
	}

	public void setInfadicCamadaInf(String infadicCamadaInf) {
		this.infadicCamadaInf = infadicCamadaInf;
	}

	@Transient
	public String escalaAjustada() {
		if (ajusteDeEscala.contains(getIdentcdgEscala())) {
			return "";
		} else {
			return getIdentcdgEscala().replace(":", ": ");
		}
	}

	@Transient
	public String observacaoEscala() {
		if (escalaAjustada().equals("")) {
			if (getIdentcdgEscala().equals("0")) {
				return "Escala não informada";
			} else {
				return "Escala: " + getIdentcdgEscala();
			}
		} else {
			return "";
		}
	}
	
	@Transient
	public String qualidadeLinhagemAjustada() {
        return (getQualidadeLinhagem().trim().equals("") ? "Não informado" : getQualidadeLinhagem());
	}
}
