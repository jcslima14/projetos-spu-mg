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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
