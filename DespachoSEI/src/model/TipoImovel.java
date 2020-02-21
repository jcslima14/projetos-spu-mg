import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import framework.ItemComboBox;

@Entity
@Table(name = "tipoimovel")
public class TipoImovel implements ItemComboBox {
	
	static int URBANO_ID = 1;
	static int RURAL_ID = 2;

	static TipoImovel URBANO = new TipoImovel(URBANO_ID, "Urbano");
	static TipoImovel RURAL = new TipoImovel(RURAL_ID, "Rural");

	@SuppressWarnings("serial")
	static List<TipoImovel> TIPOS_IMOVEIS = new ArrayList<TipoImovel>() {{ 
			add(URBANO); 
			add(RURAL); 
		}};

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer tipoImovelId;
	
	private String descricao;

	public TipoImovel() {
	}
	
	public TipoImovel(Integer tipoImovel, String descricao) {
		this.tipoImovelId = tipoImovel;
		this.descricao = descricao;
	}

	public TipoImovel(Integer tipoImovel) {
		this.tipoImovelId = tipoImovel;
	}

	public Integer getTipoImovelId() {
		return tipoImovelId;
	}

	public void setTipoImovelId(Integer tipoImovelId) {
		this.tipoImovelId = tipoImovelId;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Override
	public Integer getIntegerItemValue() {
		return getTipoImovelId();
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
