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

	public void acessarRaizProcesso(String numeroProcesso) {
		driver.switchTo().frame("ifrArvore");
		WebElement lnkNumeroProcesso = encontrarElemento(By.xpath("//a/span[contains(text(), '" + numeroProcesso + "')]"));
		lnkNumeroProcesso.click();
		driver.switchTo().defaultContent();
	}
	
	public String inserirDocumentoNoProcesso(String numeroProcesso, String tipoDocumento, String documentoModelo) throws Exception {
		acessarRaizProcesso(numeroProcesso);
		return inserirDocumentoNativo(tipoDocumento, documentoModelo);
	}
	
	private void inserirDocumento(String tipoDocumento) throws Exception {
		driver.switchTo().frame("ifrVisualizacao");
		// incluir os documentos no processo
		WebElement btnIncluirDocumento = null;
		try {
			btnIncluirDocumento = encontrarElemento(By.xpath("//img[@alt = 'Incluir Documento']"));
		} catch (Exception e) {
			throw new Exception("Não foi encontrado o botão de incluir documentos no processo. Verifique se este processo está aberto. Se não estiver, reabra-o e processe novamente.");
		}
		btnIncluirDocumento.click();

		// clica no tipo de documento
		WebElement btnOpcaoTipoDocumento = encontrarElemento(By.xpath("//a[text() = '" + tipoDocumento + "']"));
		btnOpcaoTipoDocumento.click();

		// seleciona nivel de acesso do documento - somente público por enquanto
		selecionarVisibilidadeDocumento();
	}

	private void salvarDocumento() throws InterruptedException {
		// clica em confirmar dados
		WebElement btnConfirmarDados = encontrarElemento(By.xpath("//button[@id = 'btnSalvar']"));
		btnConfirmarDados.click();
	}

	private String editarDocumentoNativo() throws Exception {
		// esperar abrir a janela popup
		do {
			TimeUnit.SECONDS.sleep(1);
		} while (driver.getWindowHandles().size() == 1);
		
		// abriu janela para editar o documento, então navega até a janela
		for (String tituloJanela : driver.getWindowHandles()) {
			driver.switchTo().window(tituloJanela);
		}

		return driver.getTitle().split(" - ")[1];
	}
	
	public void substituirMarcacaoDocumento(Map<String, String> mapaSubstituicoes) throws Exception {
		// volta ao conteúdo default
		driver.switchTo().defaultContent();

		// clica no botão localizar
		WebElement btnSubstituir = encontrarElemento(By.xpath("//div[contains(@id, 'cke_txaEditor') and contains(@class, 'cke_detached') and not(contains(@style, 'display: none'))]//a[@title = 'Substituir']"));
		btnSubstituir.click();
		TimeUnit.MILLISECONDS.sleep(500);

		// repetir este pedaço para todos os textos a serem substituídos no documento
		for (String chave : mapaSubstituicoes.keySet()) {
			String textoSubstituto = mapaSubstituicoes.get(chave);

			// preenche o texto a ser encontrado
			WebElement txtPesquisar = encontrarElemento(By.xpath("(//div[@role= 'dialog' and not(contains(@style, 'display: none'))]//div[@role = 'tabpanel' and not(contains(@style, 'display: none'))]//input[@type = 'text'])[1]"));
			txtPesquisar.clear();
			txtPesquisar.sendKeys(chave);
			
			// preenche o texto para substituição
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
		
		// aguarda até que o botão de salvar esteja novamente desabilitado para fechar a janela
		encontrarElemento(By.xpath("//div[contains(@id, 'cke_txaEditor') and contains(@class, 'cke_detached') and not(contains(@style, 'display: none'))]//a[contains(@title, 'Salvar') and @aria-disabled]"));

		driver.close();
		driver.switchTo().window(janelaPrincipal);
	}
	
	public void incluirDocumentoBlocoAssinatura(String numeroDocumento, String blocoAssinatura) {
		driver.switchTo().frame("ifrVisualizacao");
		WebElement btnIncluirBlocoAssinatura = encontrarElemento(By.xpath("//img[@alt = 'Incluir em Bloco de Assinatura']"));
		btnIncluirBlocoAssinatura.click();

		// seleciona o bloco interno desejado
		Select cbxBlocoAssinatura = new Select(encontrarElemento(By.id("selBloco")));
		cbxBlocoAssinatura.selectByValue(blocoAssinatura);

		// aguardar que a linha com o documento gerado seja carregada
		encontrarElemento(By.xpath("//table[@id = 'tblDocumentos']/tbody/tr[./td[2]/a[text() = '" + numeroDocumento + "']]"));

		// clica em incluir
		WebElement btnIncluir = encontrarElemento(By.id("sbmIncluir"));
		btnIncluir.click();

		// aguardar que a linha retorno indicando que o registro está inserido no bloco
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

		// complementa os dados com o número do modelo, caso tenha sido passado
		if (!documentoModelo.trim().equals("")) {
			WebElement lblDocumentoModelo = encontrarElemento(By.xpath("//label[contains(text(), 'Documento Modelo')]"));
			lblDocumentoModelo.click();
	
			// preenche o código do documento modelo
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

		// loop para esperar que o documento apareça na lista
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
		// espera aparecer o botão de consultar/alterar documento para ter certeza de que o upload terminou
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
				welAutor = encontrarElemento(by);
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
		
		// volta ao conteúdo default
		driver.switchTo().defaultContent();
	}
}
