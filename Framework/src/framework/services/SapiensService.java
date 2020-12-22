package framework.services;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileDeleteStrategy;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import framework.exceptions.MyException;
import framework.utils.MyUtils;

public class SapiensService extends SeleniumService {
	public SapiensService(String navegador, String endereco) throws Exception {
		super(navegador, endereco, true, "", 20);
	}
	
	public SapiensService(String navegador, String endereco, boolean exibirNavegador) throws Exception {
		super(navegador, endereco, exibirNavegador, "", 20);
	}
	
	public SapiensService(String navegador, String endereco, boolean exibirNavegador, String pastaDeDownload) throws Exception {
		super(navegador, endereco, exibirNavegador, pastaDeDownload, 20);
	}
	
	public SapiensService(String navegador, String endereco, boolean exibirNavegador, String pastaDeDownload, int timeoutImplicito) throws Exception {
		super(navegador, endereco, exibirNavegador, pastaDeDownload, timeoutImplicito);
	}

	public void login(String usuario, String senha) throws Exception {
		this.driver.switchTo().defaultContent();

        WebElement weUsuario = driver.findElement(By.xpath("//input[@name = 'username']"));
        espera(10, 1).until(ExpectedConditions.elementToBeClickable(weUsuario));
        weUsuario.sendKeys(usuario);

        WebElement weSenha = driver.findElement(By.name("password"));
        weSenha.sendKeys(senha);

        // Find the text input element by its name
        WebElement botaoAcessar = driver.findElement(By.xpath("//span[text() = 'Entrar']"));
        botaoAcessar.click();

        fecharPopup();

        this.janelaPrincipal = driver.getWindowHandle();
	}

	public void clicarAbaOficios() throws Exception {
        WebElement abaOficios = encontrarElemento(5, 1, By.xpath("//a[.//span[text() = 'Ofícios']]"));
        moverMouseParaElementoEClicar(abaOficios);
        TimeUnit.SECONDS.sleep(2);
        abaOficios.click();
	}
	
	public WebElement obterTabelaProcessos() throws Exception {
		esperarCarregamento(1000, 5, 1, "//div[text() = 'Carregando...']");

		TimeUnit.SECONDS.sleep(2);

        // obtem a lista de processos a ser lida
        WebElement tabela = encontrarElemento(5, 1, By.xpath("//div[@id = 'comunicacaoGrid-body']//table"));
        return tabela;
	}
	
	public List<WebElement> obterProcessos(WebElement element) {
        List<WebElement> linhas = element.findElements(By.xpath("./tbody/tr[.//*[text() = 'SOLICITAÇÃO DE SUBSÍDIOS' or text() = 'REITERAÇÃO DE SOLICITAÇÃO DE SUBSÍDIOS' or text() = 'COMPLEMENTAÇÃO DE SOLICITAÇÃO DE SUBSÍDIOS']]"));
        return linhas;
	}

	public String[] obterInformacoesProcesso(WebElement linha, WebElement tabela) {
		List<WebElement> links = linha.findElements(By.xpath(".//a"));
		moverMouseParaElemento(links.get(links.size()-1));
		String nup = links.get(0).getText();
		String processoJudicial = linha.findElement(By.xpath("./td[3]/div")).getText().split("\\(")[0].trim();
		String especie = linha.findElement(By.xpath("./td[4]/div")).getText().trim();
		String dataHora = linha.findElement(By.xpath("./td[7]/div")).getText();

		return new String[] { nup, processoJudicial, especie, dataHora };
	}

	public String obterNomeAutorProcesso() {
    	String autor;
    	try {
    		autor = encontrarElemento(60, 2, By.xpath("//div[@id = 'dadosInteressadosFC-innerCt']//table[contains(@class, 'x-grid-table')]/tbody/tr[./td/div[text() = 'REQUERENTE (PÓLO ATIVO)']]/td[2]")).getText();
    	} catch (Exception e) {
    		autor = "";
    	}
    	
    	return autor;
	}
	
	public void baixarProcesso(WebElement linha, boolean baixarTodoProcessoSapiens, String pastaDeDownload, String especie, String nup, String numeroProcessoJudicial, String dataRemessa) throws Exception {
    	// obtem a lista de juntadas a baixar (se for para baixar todo o processo, a lista retornará vazia)
		List<String> juntadas = obterJuntadasABaixar(baixarTodoProcessoSapiens, especie);

    	if (baixarTodoProcessoSapiens || juntadas.size() > 0) {
    		// inicia o download dos documentos
			preparaPastaProcesso(pastaDeDownload, numeroProcessoJudicial, dataRemessa);

			// clica no ícone de download de arquivos
			WebElement btnDownloadDocumentos = encontrarElemento(5, 1, By.xpath("//a[@data-qtip = 'Download dos Documentos']"));
			moverMouseParaElementoEClicar(btnDownloadDocumentos);

			selecionarDownloadParcial(juntadas);
			aguardaTerminoDownload(pastaDeDownload, nup, numeroProcessoJudicial, dataRemessa, especie.startsWith("COMPLEMENTAÇÃO"));

			// clica no botão fechar
			WebElement btnFechar = encontrarElemento(5, 1, By.xpath("//a[./span/span/span[text() = 'Fechar']]"));
			moverMouseParaElementoEClicar(btnFechar);
		} else {
			throw new MyException("Não foram encontrados os documentos para serem baixados.");
		}
	}
	
	private boolean aguardaTerminoDownload(String pastaDeDownload, String nup, String numeroProcessoJudicial, String dataRemessa, boolean isComplementacao) throws Exception {
		boolean arquivosOk = false;
		int nTentativas = 0;

		do {
			String tituloJanelaAtual = driver.getWindowHandle();
			int quantidadeJanelas = driver.getWindowHandles().size();

			// clica no botão para gerar o PDF
			WebElement btnGerar = encontrarElemento(5, 1, By.xpath("//a[./span/span/span[text() = 'Gerar']]"));
			moverMouseParaElementoEClicar(btnGerar);

			if (ocorreuErroDownload(tituloJanelaAtual, quantidadeJanelas)) {
				break;
			}
			
    		// após clicar nos links, renomear os arquivos e atualizar as informações para processamento final dos arquivos
    		arquivosOk = arquivosBaixadosERenomeados(pastaDeDownload, nup, numeroProcessoJudicial, dataRemessa, isComplementacao);
		} while (!arquivosOk && ++nTentativas < 3);

		if (!arquivosOk && nTentativas >= 3) {
			throw new MyException("Após 3 tentativas, não foi possível realizar o download dos arquivos do processo.");
		}
		
		return arquivosOk;
	}

	private void selecionarDownloadParcial(List<String> juntadas) throws Exception {
		if (juntadas.size() == 0) return;

		// encontra o objeto de marcar download parcial
		WebElement chkParcial = encontrarElemento(5, 1, By.xpath("//div[contains(@id, 'edicaodownloadwindow')]//input[@type = 'button' and @role = 'checkbox']"));
		chkParcial.click();

		TimeUnit.SECONDS.sleep(1);

		// preenche o campo de quais documentos imprimir
		WebElement txtDocsAImprimir = encontrarElemento(5, 1, By.xpath("//input[@name = 'parcial']"));

		txtDocsAImprimir.sendKeys(String.join(",", juntadas));
	}

	private List<String> obterJuntadasABaixar(boolean baixarTodoProcessoSapiens, String especie) throws Exception {
		List<String> juntadas = new ArrayList<String>();
		// se for para baixar todo o processo do Sapiens, não precisa identificar as juntadas individuais
		if (baixarTodoProcessoSapiens) return juntadas;

		boolean encontrouRemessaDocumentos = false;
		boolean isComplementacao = especie.startsWith("COMPLEMENTAÇÃO");
		boolean encontrouComplementacao = !isComplementacao; // se não for complementação, não precisa encontrar a remessa de documentos referente a esta espécie de movimentação

		Integer seqAnterior = -1;
		int registroInicialEsperado = 1;

    	do {
    		esperarCarregamento(100, 5, 1, "//div[text() = 'Carregando...']");
    		// aguardar a lista de documentos ser carregada
    		encontrarElemento(60, 1, By.xpath("//fieldset[@id = 'dadosDocumentosFC']//b[contains(text(), '" + registroInicialEsperado + " à ')]"));

    		// obter a quantidade de registros da tabela
    		aguardarCargaListaDocumentos("//fieldset[@id = 'dadosDocumentosFC']//table[contains(@class, 'x-grid-table')]/tbody/tr[./td[1][./div[contains(text(), ' (')]]]",
    				obterQuantidadeDocumentosEsperados(5, 1, By.xpath("//fieldset[@id = 'dadosDocumentosFC']//div[contains(@id, 'xtoolbar')]//div/*[contains(text(), 'registro(s)')]")));
    		
        	List<WebElement> regDocumentos = encontrarElementos(5, 1, By.xpath("//fieldset[@id = 'dadosDocumentosFC']//table[contains(@class, 'x-grid-table')]/tbody/tr[./td[1][./div[contains(text(), ' (')]]]"));
        	for (WebElement regDocumento : regDocumentos) {
        		// busca os dados a serem registrados
        		String movimento = regDocumento.findElement(By.xpath("./td[3]")).getText();
        		String dataHoraDocumento = regDocumento.findElement(By.xpath("./td[2]")).getText();
        		Integer seqDocInicial = Integer.parseInt(regDocumento.findElement(By.xpath("./td[1]")).getText().split(" ")[0]);
        		Integer seqDocFinal = null;
        		if (seqAnterior.equals(-1)) {
        			seqDocFinal = seqDocInicial.intValue() + regDocumento.findElements(By.xpath("./td[4]//i[@class = 'icon-link']")).size();
        		} else {
        			seqDocFinal = seqAnterior.intValue() - 1;
        		}

        		seqAnterior = seqDocInicial;
        		
        		// busca os sequencias da remessa de comunicação referente à espécie da solicitação e a remessa seguinte de solicitação de subsídios ou reiteração
        		if ((movimento.toUpperCase().startsWith(especie + " - REMESSA DE COMUNICAÇÃO (") && dataHoraDocumento.startsWith(dataHoraDocumento))
        				|| (isComplementacao 
        						&& encontrouRemessaDocumentos 
        						&& (movimento.toUpperCase().startsWith("SOLICITAÇÃO DE SUBSÍDIOS - REMESSA DE COMUNICAÇÃO") || movimento.toUpperCase().startsWith("REITERAÇÃO DE SOLICITAÇÃO DE SUBSÍDIOS - REMESSA DE COMUNICAÇÃO")))) {
        			// adiciona a faixa de documentos encontrados aos que devem ser baixados
        			juntadas.add(seqDocInicial.toString() + "-" + seqDocFinal.toString());
        			if (movimento.toUpperCase().startsWith(especie + " - REMESSA DE COMUNICAÇÃO (") && dataHoraDocumento.startsWith(dataHoraDocumento)) {
        				encontrouRemessaDocumentos = true;
        			} else {
        				encontrouComplementacao = true;
        			}
        		}

        		if (encontrouRemessaDocumentos && encontrouComplementacao) break;
        	} // fim do loop das linhas de documentos

    		if (encontrouRemessaDocumentos && encontrouComplementacao) break;

    		// se ainda existem páginas a ler, busca a próxima página
	        try {
	        	WebElement btnProximaPagina = encontrarElemento(5, 1, By.xpath("//fieldset[@id = 'dadosDocumentosFC']//a[@data-qtip = 'Próxima Página' and not(contains(@class, 'x-item-disabled'))]"));
	        	moverMouseParaElementoEClicar(btnProximaPagina);
	        	registroInicialEsperado += 25;
	        } catch (Exception e) {
		        break;
	        }
    	} while (!encontrouRemessaDocumentos || !encontrouComplementacao);
    	
    	return juntadas;
	}

	public void abrirProcesso(WebElement linha) throws Exception {
    	WebElement lnkProcesso = linha.findElement(By.xpath("./td[2]//i[@data-qtip = 'Processo']//following-sibling::a"));
		lnkProcesso.click();
		mudaFocoParaPopup(1);
	}
	
	private int obterQuantidadeDocumentosEsperados(int timeout, int pollingEvery, By by) throws Exception {
		WebElement capQuantidadeRegistros = null;
		try {
			capQuantidadeRegistros = encontrarElemento(timeout, pollingEvery, by);
		} catch (Exception e) {
			throw new MyException("A tabela pesquisada não possui nenhum documento.");
		}
		String quantidadeRegistros = capQuantidadeRegistros.getText();
		return Integer.parseInt(quantidadeRegistros.split(" ")[2]) - Integer.parseInt(quantidadeRegistros.split(" ")[0]) + 1;
	}

	private void preparaPastaProcesso(String caminho, String processo, String dataHora) throws Exception {
		apagaPastaDeDownloads(caminho);
		File diretorio = new File(caminho + File.separator + processo + " (" + MyUtils.formatarData(MyUtils.obterData(dataHora, "dd-MM-yyyy HH:mm"), "yyyyMMdd_HHmm") + ")");
		if (!diretorio.exists()) {
			diretorio.mkdir();
		} else {
			for (File arquivo : diretorio.listFiles()) {
				arquivo.delete();
			}
		}
	}

	private boolean ocorreuErroDownload(String tituloJanelaAtual, int quantidadeJanelas) throws Exception {
		boolean retorno = false;
		boolean janelaAberta = false;
		String tituloJanelaAberta = null;

		// espera 1 segundo pela abertura da janela de download
		TimeUnit.MILLISECONDS.sleep(2000);
		
		// se abriu uma janela, aguarda enquanto ela estiver carregando
		if (driver.getWindowHandles().size() > quantidadeJanelas) {
			try {
				driver.switchTo().window(driver.getWindowHandles().toArray()[driver.getWindowHandles().size() - 1].toString());
				janelaAberta = true;
			} catch (Exception e) {
			} finally {
				if (janelaAberta) {
					do {
						try {
							tituloJanelaAberta = driver.getTitle();
						} catch (NoSuchWindowException nswe) {
							break;
						}
						if (!tituloJanelaAberta.toLowerCase().startsWith("Carregando")) {
							break;
						}

						TimeUnit.SECONDS.sleep(1);
					} while (true);
				}
			}
		}

		if (janelaAberta && tituloJanelaAberta != null && !tituloJanelaAberta.trim().equals("")) {
			WebElement msgErro = null;
			try {
				msgErro = encontrarElemento(60, 1, By.xpath("//p[contains(text(), 'Não foi possível gerar o PDF')]"));
			} catch (Exception e) {
			}
			
			if (msgErro != null) {
				retorno = true;
				driver.close();
			}
		}

		driver.switchTo().window(tituloJanelaAtual);
		
		return retorno;
	}

	private boolean arquivosBaixadosERenomeados(String caminho, String nup, String numeroProcesso, String dataRemessa, boolean isComplementacao) throws Exception {
		File arquivoBaixado = null;
		String pastaProcesso = caminho + File.separator + numeroProcesso + " (" + MyUtils.formatarData(MyUtils.obterData(dataRemessa, "dd-MM-yyyy HH:mm"), "yyyyMMdd_HHmm") + ")";
		String ultimaChave = "";
		int segundosDesdeUltimaAlteracao = 0;
		FilenameFilter filtro = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				File arquivo = new File(dir, name);
				return (name != null && name.toLowerCase().startsWith(nup) && name.toLowerCase().endsWith(".pdf") && arquivo.length() > 0);
			}
		};

		do {
			segundosDesdeUltimaAlteracao++;
			File[] arquivosBaixados = (new File(caminho)).listFiles(filtro);
			if (arquivosBaixados.length > 0) {
				arquivoBaixado = arquivosBaixados[0];
			} else {
				String chaveAtual = chaveArquivos(caminho);
	
				if (!chaveAtual.equals(ultimaChave)) {
					ultimaChave = chaveAtual;
					segundosDesdeUltimaAlteracao = 0;
				}
			}
			TimeUnit.SECONDS.sleep(1);
		} while ((arquivoBaixado == null || !arquivoBaixado.exists()) && segundosDesdeUltimaAlteracao < 150);

		// se atingiu o tempo limite sem ter completado o download, retorna falso para que se tente baixar novamente os arquivos
		if (arquivoBaixado == null || !arquivoBaixado.exists()) {
			apagaPastaDeDownloads(caminho);
			return false;
		}

		if (arquivoBaixado.renameTo(new File(pastaProcesso + File.separator + numeroProcesso + (isComplementacao ? " --- COMPLEMENTAÇÃO ---" : "") + ".pdf"))) {
			apagaPastaDeDownloads(caminho);
		} else {
			return false;
		}

		return true;
	}

	private void apagaPastaDeDownloads(String caminho) {
		File pasta = new File(caminho);
		for (File arquivo : pasta.listFiles()) {
			if (!arquivo.isDirectory()) {
				FileDeleteStrategy.FORCE.deleteQuietly(arquivo);
			}
		}
	}
	
	private String chaveArquivos(String caminho) {
		String retorno = "";
		File pasta = new File(caminho);
		File[] arquivos = pasta.listFiles();
		Arrays.sort(arquivos, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		for (File arquivo : arquivos) {
			if (!arquivo.isDirectory()) {
				retorno = retorno + arquivo.getName() + "-" + arquivo.length() + ";";
			}
		}
		
		return retorno;
	}

	public boolean proximaPaginaProcessos() {
        try {
        	WebElement btnProximaPagina = encontrarElemento(5, 1, By.xpath("//a[@data-qtip = 'Próxima Página' and not(contains(@class, 'x-item-disabled'))]"));
        	moverMouseParaElementoEClicar(btnProximaPagina);
        	return true;
        } catch (Exception e) {
	        return false;
        }
	}

	public void filtrarProcesso(String tipoFiltro, String chaveBusca) throws Exception {
        WebElement cbcProcessoJudicial = encontrarElemento(5, 1, By.xpath("//div[./span[text() = '" + tipoFiltro + "']]"));
    	TimeUnit.SECONDS.sleep(1);
        executarJavaScript("arguments[0].scrollIntoView(true);", cbcProcessoJudicial);
        executarJavaScript("arguments[0].click();", cbcProcessoJudicial);
        TimeUnit.MILLISECONDS.sleep(500);

        WebElement btnExpandirMenu = encontrarElemento(5, 1, By.xpath("//div[./span[text() = '" + tipoFiltro + "']]/div"));
        executarJavaScript("arguments[0].click();", btnExpandirMenu);
        TimeUnit.MILLISECONDS.sleep(500);

        WebElement divFiltro = encontrarElemento(5, 1, By.xpath("//div[./a/span[text() = 'Filtros']]"));
    	esperarCarregamento(1000, 5, 1, "//div[text() = 'Carregando...']");

    	moverMouseParaElemento(divFiltro);
        WebElement iptPesquisar = encontrarElemento(5, 1, By.xpath("//div[not(contains(@style, 'visibility: hidden')) and contains(@class, 'x-menu-plain')]//input[@type = 'text' and @role = 'textbox' and @data-errorqtip = '' and contains(@name, 'textfield')]"));
        TimeUnit.MILLISECONDS.sleep(500);
        iptPesquisar.clear();
        iptPesquisar.sendKeys(chaveBusca);
	}

	public boolean responderProcesso(String tipoFiltro, String chaveBusca, String numeroProcesso, File arquivo) throws Exception {
        boolean encontrado = false;

        do {
        	esperarCarregamento(1000, 5, 1, "//div[text() = 'Carregando...']");
        	TimeUnit.SECONDS.sleep(1);

	        // após retorno da pesquisa, buscar tabela "//table[contains(@id, 'gridview')]"
	        List<WebElement> linhasRetornadas = encontrarElementos(5, 1, By.xpath("//table[contains(@id, 'gridview')]/tbody/tr"));

	        // se não encontrou nenhum registro, sai do loop
			if (linhasRetornadas.size() == 0) {
				break;
			}

			encontrado = true;

			validarResultadoPesquisa(linhasRetornadas.iterator().next(), tipoFiltro, chaveBusca, numeroProcesso);
			clicarResponderProcesso(linhasRetornadas.iterator().next());
        	realizarUploadArquivo(arquivo);
        	fecharJanelaUploadArquivo();

        	esperarCarregamento(500, 5, 1, "//div[text() = 'Carregando...']");

			WebElement btnAtualizar = encontrarElemento(5, 1, By.xpath("//a[@data-qtip = 'Atualizar']"));
			moverMouseParaElementoEClicar(btnAtualizar);
        } while (true);

    	esperarCarregamento(500, 5, 1, "//div[text() = 'Carregando...']");
        
        return encontrado;
	}

	private void clicarResponderProcesso(WebElement linha) throws Exception {
		WebElement colID = linha.findElement(By.xpath("./td[1]/div"));
		moverMouseParaElementoEClicarBotaoDireito(colID);

    	TimeUnit.SECONDS.sleep(1);

		// clicar no botão responder
		WebElement divResponder = encontrarElemento(5, 1, By.xpath("//div[./a/span[text() = 'Responder']]"));
		moverMouseParaElementoEClicar(divResponder);

    	TimeUnit.SECONDS.sleep(1);

	}

	private void fecharJanelaUploadArquivo() {
		WebElement btnFechar = encontrarElemento(5, 1, By.xpath("//a[.//span[contains(text(), 'Fechar')]]"));
		moverMouseParaElementoEClicar(btnFechar);
		
		try {
			WebElement btnNao = encontrarElemento(5, 1, By.xpath("//a[.//span[contains(@class, 'x-btn-inner') and text() = 'Não']]"));
			moverMouseParaElementoEClicar(btnNao);
		} catch (Exception e) {
		}
	}

	private void realizarUploadArquivo(File arquivo) throws Exception {
		// clicar no botão de upload de arquivos
		WebElement btnUploadArquivo = encontrarElemento(5, 1, By.id("button_browse-button"));
		moverMouseParaElemento(btnUploadArquivo);

		WebElement inpUploadArquivo = encontrarElemento(5, 1, By.xpath("//input[@type = 'file']"));
		inpUploadArquivo.sendKeys(arquivo.getAbsolutePath());

		WebElement btnConfirmarUpload = encontrarElemento(5, 1, By.id("button_upload"));
		moverMouseParaElementoEClicar(btnConfirmarUpload);

		WebElement infUploadCompleto = null;

		do {
			infUploadCompleto = encontrarElemento(5, 1, By.xpath("//tbody/tr/td[7]/div[text() = '100%']"));
		} while (infUploadCompleto == null);

    	TimeUnit.SECONDS.sleep(1);
	}

	private void validarResultadoPesquisa(WebElement linha, String tipoFiltro, String chaveBusca, String numeroProcesso) throws Exception {
		WebElement colNUP = linha.findElement(By.xpath("./td[2]/div//a"));
		WebElement colNumeroProcessoJudicial = linha.findElement(By.xpath("./td[3]/div"));
		String nup = colNUP.getText().trim().replaceAll("\\D+", "");
		String numeroProcessoJudicial = colNumeroProcessoJudicial.getText().trim().replaceAll("\\D+", "");

		// se a linha retornada não corresponde à pesquisa, sai do loop
		if (tipoFiltro.equalsIgnoreCase("nup") && (!nup.startsWith(chaveBusca) || !colNUP.getAttribute("href").trim().toLowerCase().contains("nup=" + chaveBusca))) {
			throw new MyException("O NUP retornado (" + nup + ") não corresponde à chave de busca pesquisada (" + chaveBusca + ")");
		}

		if (!numeroProcessoJudicial.trim().equals("") && !numeroProcessoJudicial.startsWith(numeroProcesso)) {
			throw new MyException("O Processo Judicial retornado (" + numeroProcessoJudicial + ") não corresponde ao processo contido no nome do arquivo (" + numeroProcesso + ")");
		}
	}
}
