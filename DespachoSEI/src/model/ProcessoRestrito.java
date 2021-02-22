package model;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "processorestricao")
public class ProcessoRestrito {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer processoRestricaoId;
	
	private String processoJudicial;
	
	private String processoSEI;
	
	private String dataHoraProcessamento;
	
	private String resultadoProcessamento;

	public Integer getProcessoRestricaoId() {
		return processoRestricaoId;
	}

	public void setProcessoRestricaoId(Integer processoRestricaoId) {
		this.processoRestricaoId = processoRestricaoId;
	}

	public String getProcessoJudicial() {
		return processoJudicial;
	}

	public void setProcessoJudicial(String processoJudicial) {
		this.processoJudicial = processoJudicial;
	}

	public String getProcessoSEI() {
		return processoSEI;
	}

	public void setProcessoSEI(String processoSEI) {
		this.processoSEI = processoSEI;
	}

	public String getDataHoraProcessamento() {
		return dataHoraProcessamento;
	}

	public void setDataHoraProcessamento(String dataHoraProcessamento) {
		this.dataHoraProcessamento = dataHoraProcessamento;
	}

	public String getResultadoProcessamento() {
		return resultadoProcessamento;
	}

	public void setResultadoProcessamento(String resultadoProcessamento) {
		this.resultadoProcessamento = resultadoProcessamento;
	}
}
