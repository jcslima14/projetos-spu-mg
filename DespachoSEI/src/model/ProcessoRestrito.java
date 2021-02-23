package model;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "processorestrito")
public class ProcessoRestrito {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer processoRestritoId;
	
	private String processoJudicial;
	
	private String processoSEI;
	
	private Boolean processoReaberto;
	
	private Boolean processoAlterado;
	
	private String dataHoraProcessamento;
	
	private String resultadoProcessamento;

	public Integer getProcessoRestritoId() {
		return processoRestritoId;
	}

	public void setProcessoRestritoId(Integer processoRestricaoId) {
		this.processoRestritoId = processoRestricaoId;
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

	public Boolean getProcessoReaberto() {
		return processoReaberto;
	}

	public void setProcessoReaberto(Boolean processoReaberto) {
		this.processoReaberto = processoReaberto;
	}

	public Boolean getProcessoAlterado() {
		return processoAlterado;
	}

	public void setProcessoAlterado(Boolean processoAlterado) {
		this.processoAlterado = processoAlterado;
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
