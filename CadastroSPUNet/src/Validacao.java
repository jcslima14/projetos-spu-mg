import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "validacao")
public class Validacao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer validacaoId;
	
	private String nomeArquivo;
	
	private String identTituloProduto;
	
	private String identDataCriacao;
	
	private String identResumo;
	
	private String sisrefDatum;
	
	private String sisrefProjecao;
	
	private String sisrefObservacao;
	
	private String identcdgTipoReprEspacial;
	
	private String identcdgEscala;
	
	private String identcdgCategoria;
	
	private String identcdgMunicipio;
	
	private String qualidadeLinhagem;
	
	private String infadicCamadaInf; 
	
	private String status;

	public Integer getValidacaoId() {
		return validacaoId;
	}

	public void setValidacaoId(Integer validacaoId) {
		this.validacaoId = validacaoId;
	}

	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
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

	public String getIdentResumo() {
		return identResumo;
	}

	public void setIdentResumo(String identResumo) {
		this.identResumo = identResumo;
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

	public String getIdentcdgCategoria() {
		return identcdgCategoria;
	}

	public void setIdentcdgCategoria(String identcdgCategoria) {
		this.identcdgCategoria = identcdgCategoria;
	}

	public String getIdentcdgMunicipio() {
		return identcdgMunicipio;
	}

	public void setIdentcdgMunicipio(String identcdgMunicipio) {
		this.identcdgMunicipio = identcdgMunicipio;
	}

	public String getQualidadeLinhagem() {
		return qualidadeLinhagem;
	}

	public void setQualidadeLinhagem(String qualidadeLinhagem) {
		this.qualidadeLinhagem = qualidadeLinhagem;
	}

	public String getInfadicCamadaInf() {
		return infadicCamadaInf;
	}

	public void setInfadicCamadaInf(String infadicCamadaInf) {
		this.infadicCamadaInf = infadicCamadaInf;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String atribuirValor(String valorAnterior, String valorNovo) {
		if (!valorAnterior.equals(valorNovo)) {
			return valorNovo;
		} else {
			return null;
		}
	}
}
