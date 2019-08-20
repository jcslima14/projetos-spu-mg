
public class Origem {
	
	static int SAPIENS_ID = 1;
	static int SPUNET_ID = 2;

	static Origem SAPIENS = new Origem(SAPIENS_ID, null);
	static Origem SPUNET = new Origem(SPUNET_ID, null);
	
	Integer origemId;
	
	String descricao;

	public Origem(Integer origemId, String descricao) {
		this.origemId = origemId;
		this.descricao = descricao;
	}

	public Origem(Integer origemId) {
		this.origemId = origemId;
	}

	public Integer getOrigemId() {
		return origemId;
	}

	public void setOrigemId(Integer origemId) {
		this.origemId = origemId;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
}
