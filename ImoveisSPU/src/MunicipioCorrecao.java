import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "municipiocorrecao")
public class MunicipioCorrecao {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer municipioCorrecaoId;
	
	String nomeIncorreto;
	
	String nomeCorreto;

	public Integer getMunicipioCorrecaoId() {
		return municipioCorrecaoId;
	}

	public void setMunicipioCorrecaoId(Integer municipioCorrecaoId) {
		this.municipioCorrecaoId = municipioCorrecaoId;
	}

	public String getNomeIncorreto() {
		return nomeIncorreto;
	}

	public void setNomeIncorreto(String nomeIncorreto) {
		this.nomeIncorreto = nomeIncorreto;
	}

	public String getNomeCorreto() {
		return nomeCorreto;
	}

	public void setNomeCorreto(String nomeCorreto) {
		this.nomeCorreto = nomeCorreto;
	}
}
