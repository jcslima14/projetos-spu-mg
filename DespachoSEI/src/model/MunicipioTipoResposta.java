import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "municipiotiporesposta")
public class MunicipioTipoResposta {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer municipioTipoRespostaId;
	
	@ManyToOne
	@JoinColumn(name = "municipioid")
	private Municipio municipio;
	
	@ManyToOne
	@JoinColumn(name = "origemid")
	private Origem origem;

	@ManyToOne
	@JoinColumn(name = "tiporespostaid")
	private TipoResposta tipoResposta;

	public MunicipioTipoResposta() {
	}
	
	public MunicipioTipoResposta(Integer municipioTipoRespostaId, Municipio municipio, Origem origem, TipoResposta tipoResposta) {
		this.municipioTipoRespostaId = municipioTipoRespostaId;
		this.municipio = municipio;
		this.origem = origem;
		this.tipoResposta = tipoResposta;
	}

	public Integer getMunicipioTipoRespostaId() {
		return municipioTipoRespostaId;
	}

	public void setMunicipioTipoRespostaId(Integer municipioTipoRespostaId) {
		this.municipioTipoRespostaId = municipioTipoRespostaId;
	}

	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
	}

	public Origem getOrigem() {
		return origem;
	}

	public void setOrigem(Origem origem) {
		this.origem = origem;
	}

	public TipoResposta getTipoResposta() {
		return tipoResposta;
	}

	public void setTipoResposta(TipoResposta tipoResposta) {
		this.tipoResposta = tipoResposta;
	}
}
