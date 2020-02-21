import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "assinantetiporesposta")
public class AssinanteTipoResposta {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer assinanteTipoRespostaId;
	
	@ManyToOne
	@JoinColumn(name = "assinanteid")
	private Assinante assinante;
	
	@ManyToOne
	@JoinColumn(name = "tiporespostaid")
	private TipoResposta tipoResposta;
	
	private String blocoAssinatura;

	public AssinanteTipoResposta() {
	}

	public AssinanteTipoResposta(Integer assinanteTipoRespostaId, Assinante assinante, TipoResposta tipoResposta, String blocoAssinatura) {
		this.assinanteTipoRespostaId = assinanteTipoRespostaId;
		this.assinante = assinante;
		this.tipoResposta = tipoResposta;
		this.blocoAssinatura = blocoAssinatura;
	}

	public Integer getAssinanteTipoRespostaId() {
		return assinanteTipoRespostaId;
	}

	public void setAssinanteTipoRespostaId(Integer assinanteTipoRespostaId) {
		this.assinanteTipoRespostaId = assinanteTipoRespostaId;
	}

	public Assinante getAssinante() {
		return assinante;
	}

	public void setAssinante(Assinante assinante) {
		this.assinante = assinante;
	}

	public TipoResposta getTipoResposta() {
		return tipoResposta;
	}

	public void setTipoResposta(TipoResposta tipoResposta) {
		this.tipoResposta = tipoResposta;
	}

	public String getBlocoAssinatura() {
		return blocoAssinatura;
	}

	public void setBlocoAssinatura(String blocoAssinatura) {
		this.blocoAssinatura = blocoAssinatura;
	}
}
