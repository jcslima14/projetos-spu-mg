import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "estruturaplanilha")
public class EstruturaPlanilha implements Cloneable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer estruturaPlanilhaId;
	
	@ManyToOne
	@JoinColumn(name = "tipoPlanilhaId")
	private TipoPlanilha tipoPlanilha;
	
	private String nomeCampo;

	private String nomeColuna;
	
	private Boolean obrigatorio;
	
	@Transient
	private int numeroColuna = -1;

	public Integer getEstruturaPlanilhaId() {
		return estruturaPlanilhaId;
	}

	public void setEstruturaPlanilhaId(Integer estruturaPlanilhaId) {
		this.estruturaPlanilhaId = estruturaPlanilhaId;
	}

	public TipoPlanilha getTipoPlanilha() {
		return tipoPlanilha;
	}

	public void setTipoPlanilha(TipoPlanilha tipoPlanilha) {
		this.tipoPlanilha = tipoPlanilha;
	}

	public String getNomeCampo() {
		return nomeCampo;
	}

	public void setNomeCampo(String nomeCampo) {
		this.nomeCampo = nomeCampo;
	}

	public String getNomeColuna() {
		return nomeColuna;
	}

	public void setNomeColuna(String nomeColuna) {
		this.nomeColuna = nomeColuna;
	}

	public Boolean getObrigatorio() {
		return obrigatorio;
	}

	public void setObrigatorio(Boolean obrigatorio) {
		this.obrigatorio = obrigatorio;
	}

	public String getObrigatorioSimNao() {
		return (getObrigatorio() ? "Sim" : "Não");
	}
	
	@Transient
	public int getNumeroColuna() {
		return numeroColuna;
	}

	@Transient
	public void setNumeroColuna(int numeroColuna) {
		this.numeroColuna = numeroColuna;
	}

	public String getDescricaoTipoPlanilha() {
		return getTipoPlanilha().getDescricao();
	}
	
	@Override
	protected EstruturaPlanilha clone() {
		EstruturaPlanilha clone = null;
		try {
			clone = (EstruturaPlanilha) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return clone;
	}
}
