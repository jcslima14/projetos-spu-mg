package framework.services;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

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
		return inserirDocumento(tipoDocumento, documentoModelo);
	}
	
	public String inserirDocumento(String tipoDocumento, String documentoModelo) throws Exception {
		driver.switchTo().frame("ifrVisualizacao");
		WebElement btnIncluirDocumento = encontrarElemento(By.xpath("//img[@alt = 'Incluir Documento']"));
		btnIncluirDocumento.click();

		// clica no tipo de documento
		WebElement btnOpcaoTipoDocumento = encontrarElemento(By.xpath("//a[text() = '" + tipoDocumento + "']"));
		btnOpcaoTipoDocumento.click();

		// se foi passado um modelo de documento, informa os dados nos campos apropriados
		if (!documentoModelo.trim().equals("")) {
			WebElement lblDocumentoModelo = encontrarElemento(By.xpath("//label[contains(text(), 'Documento Modelo')]"));
			lblDocumentoModelo.click();
	
			// preenche o código do documento modelo
			WebElement txtCodigoDocumentoModelo = encontrarElemento(By.xpath("//input[@id = 'txtProtocoloDocumentoTextoBase']"));
			txtCodigoDocumentoModelo.sendKeys(documentoModelo);
		}

		// seleciona nivel de acesso do documento - somente público por enquanto
		WebElement lblNivelAcessoPublico = encontrarElemento(By.xpath("//label[@id = 'lblPublico']"));
		lblNivelAcessoPublico.click();
		
		// clica em confirmar dados
		WebElement btnConfirmarDados = encontrarElemento(By.xpath("//button[@id = 'btnSalvar']"));
		btnConfirmarDados.click();

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
	
	public void salvarFecharDocumento() throws Exception {
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
}
