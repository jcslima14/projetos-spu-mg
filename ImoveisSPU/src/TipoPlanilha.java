import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tipoplanilha")
public class TipoPlanilha implements ItemComboBox {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer tipoPlanilhaId;
	
	private String descricao;
	
	private Integer linhaCabecalho;

	public Integer getTipoPlanilhaId() {
		return tipoPlanilhaId;
	}

	public void setTipoPlanilhaId(Integer tipoPlanilhaId) {
		this.tipoPlanilhaId = tipoPlanilhaId;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Integer getLinhaCabecalho() {
		return linhaCabecalho;
	}

	public void setLinhaCabecalho(Integer linhaCabecalho) {
		this.linhaCabecalho = linhaCabecalho;
	}

	@Override
	public Integer getIntegerItemValue() {
		return getTipoPlanilhaId();
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
