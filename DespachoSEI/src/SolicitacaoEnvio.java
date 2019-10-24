import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "solicitacaoenvio")
public class SolicitacaoEnvio {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer solicitacaoEnvioId;
	
	@ManyToOne
	@JoinColumn(name = "solicitacaoid")
	private Solicitacao solicitacao;
	
	private String dataHoraMovimentacao;
	
	private String resultadoDownload;
	
	private Boolean arquivosProcessados;
	
	private String resultadoProcessamento;

	public SolicitacaoEnvio() {
	}
	
	public SolicitacaoEnvio(Integer solicitacaoEnvioId, Solicitacao solicitacao, String dataHoraMovimentacao, String resultadoDownload, Boolean arquivosProcessados, String resultadoProcessamento) {
		this.solicitacaoEnvioId = solicitacaoEnvioId;
		this.solicitacao = solicitacao;
		this.dataHoraMovimentacao = dataHoraMovimentacao;
		this.resultadoDownload = resultadoDownload;
		this.arquivosProcessados = arquivosProcessados;
		this.resultadoProcessamento = resultadoProcessamento;
	}

	public Integer getSolicitacaoEnvioId() {
		return solicitacaoEnvioId;
	}

	public void setSolicitacaoEnvioId(Integer solicitacaoEnvioId) {
		this.solicitacaoEnvioId = solicitacaoEnvioId;
	}

	public Solicitacao getSolicitacao() {
		return solicitacao;
	}

	public void setSolicitacao(Solicitacao solicitacao) {
		this.solicitacao = solicitacao;
	}

	public String getDataHoraMovimentacao() {
		return dataHoraMovimentacao;
	}

	public void setDataHoraMovimentacao(String dataHoraMovimentacao) {
		this.dataHoraMovimentacao = dataHoraMovimentacao;
	}

	public String getResultadoDownload() {
		return resultadoDownload;
	}

	public void setResultadoDownload(String resultadoDownload) {
		this.resultadoDownload = resultadoDownload;
	}

	public Boolean getArquivosProcessados() {
		return arquivosProcessados;
	}

	public void setArquivosProcessados(Boolean arquivosProcessados) {
		this.arquivosProcessados = arquivosProcessados;
	}

	public String getResultadoProcessamento() {
		return resultadoProcessamento;
	}

	public void setResultadoProcessamento(String resultadoProcessamento) {
		this.resultadoProcessamento = resultadoProcessamento;
	}

	public String getArquivosProcessadosAsString() {
		if (getArquivosProcessados() == null) {
			return "";
		} else if (getArquivosProcessados()) {
			return "Sim";
		} else {
			return "Não";
		}
	}
}
