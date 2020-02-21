import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import framework.ItemComboBox;

@Entity
@Table(name = "tiporesposta")
public class TipoResposta implements ItemComboBox {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer tipoRespostaId;
	
	private String descricao;
	
	private String tipoDocumento;

	private String numeroDocumentoModelo;

	private Boolean gerarProcessoIndividual;
	
	private String unidadeAberturaProcesso;
	
	private String tipoProcesso;

	private Boolean imprimirResposta;
	
	private Integer quantidadeAssinaturas;
	
	@ManyToOne
	@JoinColumn(name = "origemid")
	private Origem origem;

	private String respostaSPUNet;

	private String complementoSPUNet;

	public TipoResposta() {
	}
	
	public TipoResposta(Integer tipoRespostaId) {
		this.tipoRespostaId = tipoRespostaId;
	}

	public TipoResposta(Integer tipoRespostaId, String descricao) {
		this.tipoRespostaId = tipoRespostaId;
		this.descricao = descricao;
	}

	public TipoResposta(Integer tipoRespostaId, String descricao, String tipoDocumento, String numeroDocumentoSEI, Boolean gerarProcessoIndividual, String unidadeAberturaProcesso, String tipoProcesso, 
			Boolean imprimirResposta, Integer quantidadeAssinaturas, Origem origem, String respostaSPUNet, String complementoSPUNet) {
		this.tipoRespostaId = tipoRespostaId;
		this.descricao = descricao;
		this.tipoDocumento = tipoDocumento;
		this.numeroDocumentoModelo = numeroDocumentoSEI;
		this.gerarProcessoIndividual = gerarProcessoIndividual;
		this.unidadeAberturaProcesso = unidadeAberturaProcesso;
		this.tipoProcesso = tipoProcesso;
		this.imprimirResposta = imprimirResposta;
		this.quantidadeAssinaturas = quantidadeAssinaturas;
		this.origem = origem;
		this.respostaSPUNet = respostaSPUNet;
		this.complementoSPUNet = complementoSPUNet;
	}

	public Integer getTipoRespostaId() {
		return tipoRespostaId;
	}

	public void setTipoRespostaId(Integer tipoRespostaId) {
		this.tipoRespostaId = tipoRespostaId;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public String getNumeroDocumentoModelo() {
		return numeroDocumentoModelo;
	}

	public void setNumeroDocumentoModelo(String numeroDocumentoSEI) {
		this.numeroDocumentoModelo = numeroDocumentoSEI;
	}

	public Boolean getGerarProcessoIndividual() {
		return gerarProcessoIndividual;
	}

	public void setGerarProcessoIndividual(Boolean gerarProcessoIndividual) {
		this.gerarProcessoIndividual = gerarProcessoIndividual;
	}

	public String getUnidadeAberturaProcesso() {
		return unidadeAberturaProcesso;
	}

	public void setUnidadeAberturaProcesso(String unidadeAberturaProcesso) {
		this.unidadeAberturaProcesso = unidadeAberturaProcesso;
	}

	public String getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(String tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	public Boolean getImprimirResposta() {
		return imprimirResposta;
	}

	public void setImprimirResposta(Boolean imprimirResposta) {
		this.imprimirResposta = imprimirResposta;
	}

	public Integer getQuantidadeAssinaturas() {
		return quantidadeAssinaturas;
	}

	public void setQuantidadeAssinaturas(Integer quantidadeAssinaturas) {
		this.quantidadeAssinaturas = quantidadeAssinaturas;
	}

	public Origem getOrigem() {
		return origem;
	}

	public void setOrigem(Origem origem) {
		this.origem = origem;
	}

	public String getRespostaSPUNet() {
		return respostaSPUNet;
	}

	public void setRespostaSPUNet(String respostaSPUNet) {
		this.respostaSPUNet = respostaSPUNet;
	}

	public String getComplementoSPUNet() {
		return complementoSPUNet;
	}

	public void setComplementoSPUNet(String complementoSPUNet) {
		this.complementoSPUNet = complementoSPUNet;
	}

	public String getGerarProcessoIndividualAsString() {
		if (getGerarProcessoIndividual() == null) {
			return "";
		} else if (getGerarProcessoIndividual()) {
			return "Sim";
		} else {
			return "Não";
		}
	}

	public String getImprimirRespostaAsString() {
		if (getImprimirResposta() == null) {
			return "";
		} else if (getImprimirResposta()) {
			return "Sim";
		} else {
			return "Não";
		}
	}

	@Override
	public Integer getIntegerItemValue() {
		return getTipoRespostaId();
	}

	@Override
	public String getStringItemValue() {
		return null;
	}

	@Override
	public String getItemLabel() {
		return getDescricao();
	}
}
