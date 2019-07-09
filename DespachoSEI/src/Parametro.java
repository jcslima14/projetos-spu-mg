import java.util.LinkedHashMap;
import java.util.Map;

public class Parametro {
	static int PASTA_DOWNLOAD_SAPIENS = 1;
	static int PASTA_DESTINO_PROCESSOS_SAPIENS = 2;
	static int ENDERECO_SEI = 3;
	static int ENDERECO_SAPIENS = 4;
	static int PASTA_DESPACHOS_SALVOS = 5;
	static int PASTA_PLANILHA_IMPORTACAO = 6;
	static int DEFAULT_BROWSER = 7;
	static int ENDERECO_SPUNET = 8;
	static int PASTA_ARQUIVOS_PROCESSOS_INDIVIDUAIS = 9;

	@SuppressWarnings("serial")
	static Map<Integer, String> DESCRICOES = new LinkedHashMap<Integer, String>() {{ 
		put(PASTA_DOWNLOAD_SAPIENS, "Pasta de download dos arquivos do Sapiens");
		put(PASTA_DESTINO_PROCESSOS_SAPIENS, "Pasta de destino dos processos do Sapiens");
		put(ENDERECO_SEI, "Endereço do SEI");
		put(ENDERECO_SAPIENS, "Endereço do Sapiens");
		put(PASTA_DESPACHOS_SALVOS, "Pasta de despachos impressos (salvos em PDF)");
		put(PASTA_PLANILHA_IMPORTACAO, "Pasta da planilha de importação");
		put(DEFAULT_BROWSER, "Navegador padrão");
		put(ENDERECO_SPUNET, "Endereço do SPUNet");
		put(PASTA_ARQUIVOS_PROCESSOS_INDIVIDUAIS, "Pasta dos arquivos de processos individuais");
	}};
	
	public static String obterDescricao(int parametroId) {
		return DESCRICOES.get(parametroId);
	}
	
	Integer parametroId;
	
	String descricao;
	
	String conteudo;

	public Parametro(Integer parametroId, String descricao, String conteudo) {
		this.parametroId = parametroId;
		this.descricao = descricao;
		this.conteudo = conteudo;
	}

	public Integer getParametroId() {
		return parametroId;
	}

	public void setParametroId(Integer parametroId) {
		this.parametroId = parametroId;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}
}
