package framework.services;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import framework.utils.MyUtils;

public class SEIService extends SeleniumService {
	public SEIService(String navegador, String endereco) throws Exception {
		super(navegador, endereco, true, "", 20);
	}
	
	public SEIService(String navegador, String endereco, boolean exibirNavegador) throws Exception {
		super(navegador, endereco, exibirNavegador, "", 20);
	}
	
	public SEIService(String navegador, String endereco, boolean exibirNavegador, String pastaDeDownload) throws Exception {
		super(navegador, endereco, exibirNavegador, pastaDeDownload, 20);
	}
	
	public SEIService(String navegador, String endereco, boolean exibirNavegador, String pastaDeDownload, int timeoutImplicito) throws Exception {
		super(navegador, endereco, exibirNavegador, pastaDeDownload, timeoutImplicito);
	}

	public void login(String usuario, String senha, String unidadeSEI) throws Exception {
		this.driver.switchTo().defaultContent();
		
        WebElement weUsuario = encontrarElemento(By.id("txtUsuario"));
        weUsuario.sendKeys(usuario);

        WebElement weSenha = encontrarElemento(By.id("pwdSenha"));
        weSenha.sendKeys(senha);

        Select cbxOrgao = new Select(encontrarElemento(By.id("selOrgao")));
        cbxOrgao.selectByVisibleText(unidadeSEI);

        TimeUnit.MILLISECONDS.sleep(1500);

        WebElement botaoAcessar = encontrarElemento(By.id("sbmLogin"));
        botaoAcessar.click();
        
        fecharPopup();
        
        this.janelaPrincipal = driver.getWindowHandle();
	}

	public void selecionarUnidadePadrao(String unidadePadrao) {
		if (unidadePadrao.trim().equals("")) return;
		
		driver.switchTo().defaultContent();
    	Select cbxUnidade = new Select(encontrarElemento(By.id("selInfraUnidades")));
    	cbxUnidade.selectByVisibleText(unidadePadrao);
	}

	public void pesquisarProcesso(String numeroProcesso) {
		WebElement txtPesquisaRapida = encontrarElemento(By.xpath("//input[@id = 'txtPesquisaRapida']"));
		txtPesquisaRapida.sendKeys(numeroProcesso);
		txtPesquisaRapida.sendKeys(Keys.RETURN);
	}

	public void acessarRaizProcesso(String numeroProcesso) throws Exception {
		driver.switchTo().defaultContent();
		driver.switchTo().frame("ifrArvore");
		WebElement lnkNumeroProcesso = encontrarElemento(By.xpath("//a/span[contains(text(), '" + numeroProcesso + "')]"));
		lnkNumeroProcesso.click();
		// espera retornar o bot�o incluir documento
		TimeUnit.MILLISECONDS.sleep(200);
		driver.switchTo().defaultContent();
	}

	public String inserirDocumentoNoProcesso(String numeroProcesso, String tipoDocumento, String documentoModelo) throws Exception {
		acessarRaizProcesso(numeroProcesso);
		return inserirDocumentoNativo(tipoDocumento, documentoModelo);
	}
	
	private void inserirDocumento(String tipoDocumento) throws Exception {
		// incluir os documentos no processo
		clicarBotaoAcaoProcesso("Incluir Documento");

		// clica no tipo de documento
		WebElement btnOpcaoTipoDocumento = encontrarElemento(By.xpath("//a[text() = '" + tipoDocumento + "']"));
		btnOpcaoTipoDocumento.click();

		// seleciona nivel de acesso do documento - somente p�blico por enquanto
		selecionarVisibilidadeDocumento();
	}

	private void salvarDocumento() throws InterruptedException {
		// clica em confirmar dados
		WebElement btnConfirmarDados = encontrarElemento(By.xpath("//button[@id = 'btnSalvar']"));
		btnConfirmarDados.click();
	}

	private String editarDocumentoNativo() throws Exception {
		mudaFocoParaPopup(1);
		return driver.getTitle().split(" - ")[1];
	}
	
	public void substituirMarcacaoDocumento(Map<String, String> mapaSubstituicoes) throws Exception {
		// volta ao conte�do default
		driver.switchTo().defaultContent();

		// clica no bot�o localizar
		WebElement btnSubstituir = encontrarElemento(By.xpath("//div[contains(@id, 'cke_txaEditor') and contains(@class, 'cke_detached') and not(contains(@style, 'display: none'))]//a[@title = 'Substituir']"));
		btnSubstituir.click();
		TimeUnit.MILLISECONDS.sleep(500);

		// repetir este peda�o para todos os textos a serem substitu�dos no documento
		for (String chave : mapaSubstituicoes.keySet()) {
			String textoSubstituto = mapaSubstituicoes.get(chave);

			// preenche o texto a ser encontrado
			WebElement txtPesquisar = encontrarElemento(By.xpath("(//div[@role= 'dialog' and not(contains(@style, 'display: none'))]//div[@role = 'tabpanel' and not(contains(@style, 'display: none'))]//input[@type = 'text'])[1]"));
			txtPesquisar.clear();
			txtPesquisar.sendKeys(chave);
			
			// preenche o texto para substitui��o
			WebElement txtSubstituir = encontrarElemento(By.xpath("(//div[@role= 'dialog' and not(contains(@style, 'display: none'))]//div[@role = 'tabpanel' and not(contains(@style, 'display: none'))]//input[@type = 'text'])[2]"));
			txtSubstituir.clear();
			txtSubstituir.sendKeys(textoSubstituto);
			
			// clica em substituir tudo
			WebElement btnSubstituirTudo = encontrarElemento(By.xpath("//div[@role= 'dialog' and not(contains(@style, 'display: none'))]//a[@title = 'Substituir Tudo']"));
			btnSubstituirTudo.click();
			
			// clica em ok na mensagem apresentada
			driver.switchTo().alert().accept();
		}
		
		// clica em fechar
		WebElement btnFechar = encontrarElemento(By.xpath("//div[@role= 'dialog' and not(contains(@style, 'display: none'))]//span[text() = 'Fechar']"));
		btnFechar.click();
	}
	
	public void salvarFecharDocumentoEditado() throws Exception {
		WebElement btnSalvar = encontrarElemento(By.xpath("//div[contains(@id, 'cke_txaEditor') and contains(@class, 'cke_detached') and not(contains(@style, 'display: none'))]//a[contains(@title, 'Salvar') and not(@aria-disabled)]"));
		btnSalvar.click();

		TimeUnit.MILLISECONDS.sleep(500);
		
		// aguarda at� que o bot�o de salvar esteja novamente desabilitado para fechar a janela
		encontrarElemento(By.xpath("//div[contains(@id, 'cke_txaEditor') and contains(@class, 'cke_detached') and not(contains(@style, 'display: none'))]//a[contains(@title, 'Salvar') and @aria-disabled]"));

		driver.close();
		driver.switchTo().window(janelaPrincipal);
	}
	
	public void incluirDocumentoBlocoAssinatura(String numeroDocumento, String blocoAssinatura) throws Exception {
		clicarBotaoAcaoProcesso("Incluir em Bloco de Assinatura");

		// seleciona o bloco interno desejado
		Select cbxBlocoAssinatura = new Select(encontrarElemento(By.id("selBloco")));
		cbxBlocoAssinatura.selectByValue(blocoAssinatura);

		// aguardar que a linha com o documento gerado seja carregada
		encontrarElemento(By.xpath("//table[@id = 'tblDocumentos']/tbody/tr[./td[2]/a[text() = '" + numeroDocumento + "']]"));

		// clica em incluir
		WebElement btnIncluir = encontrarElemento(By.id("sbmIncluir"));
		btnIncluir.click();

		// aguardar que a linha retorno indicando que o registro est� inserido no bloco
		encontrarElemento(By.xpath("//table[@id = 'tblDocumentos']/tbody/tr[./td[2]/a[text() = '" + numeroDocumento + "'] and ./td[5]/a[text() = '" + blocoAssinatura + "']]"));
		
		driver.switchTo().defaultContent();
	}

	public String gerarProcessoIndividual(String tipoProcesso, String descricaoProcesso) throws Exception {
		String numeroProcessoGerado = null;

		// encontrar link de iniciar processo
		WebElement lnkIniciarProcesso = encontrarElemento(By.xpath("//a[text() = 'Iniciar Processo']"));
		lnkIniciarProcesso.click();

		WebElement lnkExibirTodosTipos = null;

		try {
			lnkExibirTodosTipos = encontrarElemento(By.xpath("//img[@title = 'Exibir todos os tipos']"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (lnkExibirTodosTipos != null) {
			lnkExibirTodosTipos.click();
		}

		WebElement lnkTipoProcesso = encontrarElemento(By.xpath("//a[text() = '" + tipoProcesso + "']"));
		lnkTipoProcesso.click();

		WebElement txtDescricaoProcesso = encontrarElemento(By.id("txtDescricao"));
		txtDescricaoProcesso.sendKeys(descricaoProcesso);

		WebElement optNivelAcessoProcesso = encontrarElemento(By.id("optRestrito"));
		optNivelAcessoProcesso.click();
		TimeUnit.MILLISECONDS.sleep(200);

		Select cbxHipoteseLegal = new Select(encontrarElemento(By.id("selHipoteseLegal")));
		TimeUnit.MILLISECONDS.sleep(200);
		cbxHipoteseLegal.selectByValue("34");
		TimeUnit.MILLISECONDS.sleep(200);

		WebElement btnSalvarProcesso = encontrarElemento(By.id("btnSalvar"));
		btnSalvarProcesso.click();

		TimeUnit.SECONDS.sleep(2);
		driver.switchTo().defaultContent();

		WebElement ifrArvore = encontrarElemento(By.xpath("//iframe[@id = 'ifrArvore']"));
		driver.switchTo().frame(ifrArvore);
		WebElement txtNumeroProcesso = encontrarElemento(By.xpath("//div[@id = 'topmenu']/a[@target = 'ifrVisualizacao']"));
		numeroProcessoGerado = txtNumeroProcesso.getText();
		
		driver.switchTo().defaultContent();
		
		return numeroProcessoGerado;
	}

	public String inserirDocumentoNativo(String tipoDocumento, String documentoModelo) throws Exception {
		inserirDocumento(tipoDocumento);

		// complementa os dados com o n�mero do modelo, caso tenha sido passado
		if (!documentoModelo.trim().equals("")) {
			WebElement lblDocumentoModelo = encontrarElemento(By.xpath("//label[contains(text(), 'Documento Modelo')]"));
			lblDocumentoModelo.click();
	
			// preenche o c�digo do documento modelo
			WebElement txtCodigoDocumentoModelo = encontrarElemento(By.xpath("//input[@id = 'txtProtocoloDocumentoTextoBase']"));
			txtCodigoDocumentoModelo.sendKeys(documentoModelo);
		}
		
		salvarDocumento();
		return editarDocumentoNativo();
	}
	
	public void inserirDocumentoExterno(String tipoDocumento, File anexo) throws Exception {
		inserirDocumento(" Externo");

		// detalhes de documento externo
		Select cbxTipoDocumento = new Select(encontrarElemento(By.id("selSerie")));
		cbxTipoDocumento.selectByVisibleText("Processo");
		TimeUnit.MILLISECONDS.sleep(800);

		WebElement txtDataDocumento = encontrarElemento(By.id("txtDataElaboracao"));
		txtDataDocumento.sendKeys(MyUtils.formatarData(new Date(), "dd/MM/yyyy"));

		selecionarFormatoDocumento();
		
		// realiza o upload do arquivo
		WebElement updArquivo = encontrarElemento(By.id("filArquivo"));
		updArquivo.sendKeys(anexo.getAbsolutePath());

		// loop para esperar que o documento apare�a na lista
		WebElement divDocumentoNaLista = null;
		do {
			try {
				divDocumentoNaLista = encontrarElemento(By.xpath("//table[@id = 'tblAnexos']//tr/td/div[text() = '" + anexo.getName() + "']"));
			} catch (Exception e) {
				divDocumentoNaLista = null;
			}
		} while (divDocumentoNaLista == null);
		
		salvarDocumento();

		TimeUnit.MILLISECONDS.sleep(1500);
		// espera aparecer o bot�o de consultar/alterar documento para ter certeza de que o upload terminou
		encontrarElemento(By.xpath("//img[@title = 'Consultar/Alterar Documento Externo']"));
	}
	
	private void selecionarFormatoDocumento() {
		WebElement optNatoDigital = encontrarElemento(By.id("optNato"));
		optNatoDigital.click();
	}

	private void selecionarVisibilidadeDocumento() {
		WebElement optNivelAcessoDocumento = encontrarElemento(By.id("optPublico"));
		optNivelAcessoDocumento.click();
	}
	
	public void anexarArquivosProcesso(String numeroProcesso, List<File> anexos) throws Exception {
		pesquisarProcesso(numeroProcesso);

		for (File anexo : anexos) {
			inserirDocumentoExterno("Processo", anexo);
			acessarRaizProcesso(numeroProcesso);
		}

		driver.switchTo().defaultContent();
	}
	
	public void acessarFramePorConteudo(By by) throws Exception {
		driver.switchTo().defaultContent();
		List<WebElement> frmIFrames = null;
		int espera = 15;
		
		do {
			TimeUnit.SECONDS.sleep(2);
			frmIFrames = encontrarElementos(By.tagName("iframe"));
		} while (--espera >= 0 && (frmIFrames == null || frmIFrames.size() <= 1));
		
		WebElement welAutor = null;
		
		for (WebElement frmIFrame : frmIFrames) {
			driver.switchTo().frame(frmIFrame);

			try {
				welAutor = encontrarElemento(2, 1, by);
			} catch (Exception e) {
				welAutor = null;
			}

			if (welAutor != null) {
				break;
			} else {
				driver.switchTo().defaultContent();
			}
		}
		
		// clica no primeiro paragrafo encontrado no iframe
		welAutor.click();
		TimeUnit.SECONDS.sleep(1);
		
		// volta ao conte�do default
		driver.switchTo().defaultContent();
	}
	
	public void acessarPaginaImpressaoDocumentos() throws Exception {
		// clicar em gerar documentos
		clicarBotaoAcaoProcesso("Incluir Documento");

		// encontra a quantidade de registros aptos a serem impressos
		aguardarCargaListaDocumentos("//table[@id = 'tblDocumentos']/tbody/tr[./td[./input[@type = 'checkbox']]]", obterQuantidadeDocumentosEsperados());

		// clicar em selecionar tudo (precisa clicar 2x, pois o primeiro click marca todos (que j� est�o marcados) e o segundo desmarca tudo)
		WebElement btnDesmarcarTudo = encontrarElemento(By.xpath("//img[@title = 'Selecionar Tudo']"));
		btnDesmarcarTudo.click();
		TimeUnit.SECONDS.sleep(1);
		btnDesmarcarTudo.click();

	}
	
	private void clicarBotaoAcaoProcesso(String botao) throws Exception {
		String xpath = null;
		
		switch (botao) {
			case "Incluir Documento": xpath = "//img[@alt = 'Incluir Documento']";
			case "Imprimir Documento": xpath = "//img[@alt = 'Gerar Arquivo PDF do Processo']";
			case "Incluir em Bloco de Assinatura": xpath = "//img[@alt = 'Incluir em Bloco de Assinatura']";
		}
		
		driver.switchTo().defaultContent();
		driver.switchTo().frame("ifrVisualizacao");
		WebElement btnAcao = null;
		try {
			btnAcao = encontrarElemento(By.xpath(xpath));
		} catch (Exception e) {
			throw new Exception("N�o foi encontrado o bot�o '" + botao + "'. Verifique se este processo est� aberto. Se n�o estiver, reabra-o e processe novamente.");
		}
		btnAcao.click();
	}

	private int obterQuantidadeDocumentosEsperados() {
		String quantidadeRegistros = encontrarElemento(By.xpath("//table[@id = 'tblDocumentos']/caption")).getText();
		quantidadeRegistros = quantidadeRegistros.split("\\(")[1];
		quantidadeRegistros = quantidadeRegistros.replaceAll("\\D+", "");
		return Integer.parseInt(quantidadeRegistros);
	}

	public String imprimirDocumento(String numeroProcesso, String numeroProcessoSEI, String numeroDocumentoSEI, int quantidadeAssinaturas, String pastaDownload, String pastaDestino, String nomeArquivo) throws Exception {
		String retorno = null;
		// encontra e marca o checkbox do documento
		WebElement chkSelecionarDocumento = null;
		try {
			chkSelecionarDocumento = encontrarElemento(5, 1, By.xpath("//tr[not(contains(@class, 'infraTrMarcada')) and ./*/a[text() = '" + numeroDocumentoSEI + "']]/*/input[@class = 'infraCheckbox']"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (chkSelecionarDocumento != null) {
			// trecho para verificar se o documento possui a quantidade de assinaturas necess�rias
			WebElement lnkDocumento = encontrarElemento(5, 1, By.xpath("//a[text() = '" + numeroDocumentoSEI + "']"));
			TimeUnit.MILLISECONDS.sleep(100);
			moverMouseParaElemento(lnkDocumento);
			lnkDocumento.click();

			mudaFocoParaPopup(1);
			retorno = confereQuantidadeAssinaturasDocumento(numeroProcesso, quantidadeAssinaturas);
			
			if (retorno != null) {
				return retorno;
			} else {
				retorno = gerarDocumentoPDF(chkSelecionarDocumento, numeroProcessoSEI, numeroDocumentoSEI, pastaDownload, pastaDestino, nomeArquivo);
			}
		} else {
			retorno = "Documento n�o encontrado ou n�o habilitado para gera��o em PDF";
		}
		
		return retorno;
	}
	
	private String confereQuantidadeAssinaturasDocumento(String numeroProcesso, int quantidadeAssinaturas) {
		// espera encontrar o fim do documento para verificar se a quantidade de assinaturas est� correta
		encontrarElemento(By.xpath("//p[contains(text(), 'Processo n� " + numeroProcesso + "')]"));
		List<WebElement> assinaturas = encontrarElementos(By.xpath("//p[contains(text(), 'Documento assinado eletronicamente por')]"));

		driver.close();
		driver.switchTo().window(janelaAtual);
		driver.switchTo().defaultContent();
		driver.switchTo().frame("ifrVisualizacao");

		if (assinaturas.size() != quantidadeAssinaturas) {
			return "Para ser impresso, o documento precisa de " + quantidadeAssinaturas + " assinaturas. Este documento possui " + assinaturas.size() + " assinaturas.";
		}
		return null;
	}
	
	private String gerarDocumentoPDF(WebElement checkBoxDocumento, String numeroProcessoSEI, String numeroDocumentoSEI, String pastaDownload, String pastaDestino, String nomeArquivo) throws Exception {
		moverMouseParaElemento(checkBoxDocumento);
		checkBoxDocumento.click();

		// certifica-se de que o documento est� marcado e que somente 1 documento est� marcado na lista
		encontrarElemento(5, 1, By.xpath("//tr[contains(@class, 'infraTrMarcada') and ./*/a[text() = '" + numeroDocumentoSEI + "']]/*/input[@class = 'infraCheckbox']"));
		
		List<WebElement> documentosMarcados = encontrarElementos(5, 1, By.xpath("//tr[contains(@class, 'infraTrMarcada')]/*/input[@class = 'infraCheckbox']"));
		if (documentosMarcados.size() != 1) {
			return "A lista de documentos a serem impressos deveria ter apenas 1 documento marcado, mas est� com " + documentosMarcados.size() + " marcados. Tente imprimir o documento novamente mais tarde.";
		}

		// apaga arquivo com o nome do processo, caso j� exista
		MyUtils.apagarArquivo(pastaDownload + File.separator + "SEI_" + numeroProcessoSEI.replace("/", "_").replace("-", "_") + ".pdf", 30);

		// gera o arquivo no diret�rio de downloads
		WebElement btnGerarDocumento = encontrarElemento(5, 1, By.name("btnGerar"));
		btnGerarDocumento.click();

		String nomeArquivoDestino = pastaDestino + File.separator + nomeArquivo + ".pdf";
		MyUtils.renomearArquivo(pastaDownload + File.separator + "SEI_" + numeroProcessoSEI.replace("/", "_").replace("-", "_") + ".pdf", nomeArquivoDestino, 300, false);

		esperarCarregamento(200, 5, 1, "//div[@id = 'divInfraAvisoFundo' and contains(@style, 'visibility: visible')]//span[@id = 'spnInfraAviso']");

		aguardarCargaListaDocumentos(60, 3, "//table[@id = 'tblDocumentos']/tbody/tr[./td[./input[@type = 'checkbox']]]", obterQuantidadeDocumentosEsperados());

		checkBoxDocumento = encontrarElemento(5, 1, By.xpath("//tr[contains(@class, 'infraTrMarcada') and ./*/a[text() = '" + numeroDocumentoSEI + "']]/*/input[@class = 'infraCheckbox']"));
		checkBoxDocumento.click();
		
		// verifica se o documento est� mesmo desmcarcado
		encontrarElemento(5, 1, By.xpath("//tr[not(contains(@class, 'infraTrMarcada')) and ./*/a[text() = '" + numeroDocumentoSEI + "']]/*/input[@class = 'infraCheckbox']"));
		
		return null;
	}
	
	public String acessarBlocoAssinatura(String blocoAssinatura) {
		driver.switchTo().defaultContent();
		WebElement btnControleProcessos = encontrarElemento(5, 1, By.id("lnkControleProcessos"));
		btnControleProcessos.click();

		WebElement btnBlocosAssinatura = encontrarElemento(5, 1, By.xpath("//a[text() = 'Blocos de Assinatura']"));
		btnBlocosAssinatura.click();

		WebElement lnkBlocoAssinatura = null;

		try {
			lnkBlocoAssinatura = encontrarElemento(5, 1, By.xpath("//table[@summary = 'Tabela de Blocos.']/tbody/tr/td/a[text() = '" + blocoAssinatura + "']"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (lnkBlocoAssinatura == null) {
			return "*** N�o foi poss�vel encontrar o bloco de assinatura " + blocoAssinatura + ".";
		}

		lnkBlocoAssinatura.click();

		return null;
	}
}
