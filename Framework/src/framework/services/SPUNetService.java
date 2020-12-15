package framework.services;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import framework.MyException;
import framework.utils.MyUtils;

public class SPUNetService extends SeleniumService {
	public SPUNetService(String navegador, String endereco) throws Exception {
		super(navegador, endereco, true, "", 20);
	}
	
	public SPUNetService(String navegador, String endereco, boolean exibirNavegador) throws Exception {
		super(navegador, endereco, exibirNavegador, "", 20);
	}
	
	public SPUNetService(String navegador, String endereco, boolean exibirNavegador, String pastaDeDownload) throws Exception {
		super(navegador, endereco, exibirNavegador, pastaDeDownload, 20);
	}
	
	public SPUNetService(String navegador, String endereco, boolean exibirNavegador, String pastaDeDownload, int timeoutImplicito) throws Exception {
		super(navegador, endereco, exibirNavegador, pastaDeDownload, timeoutImplicito);
	}

	public void login(String usuario, String senha) throws Exception {
		this.driver.switchTo().defaultContent();

        WebElement weUsuario = encontrarElemento(15, 1, By.id("username"));
        espera(10, 1).until(ExpectedConditions.elementToBeClickable(weUsuario));
        weUsuario.sendKeys(usuario);

        // Find the text input element by its name
        WebElement weSenha = encontrarElemento(15, 1, By.id("password"));
        weSenha.sendKeys(senha);

        // Find the text input element by its name
        WebElement botaoAcessar = encontrarElemento(15, 1, By.xpath("//button[contains(text(), 'Acessar')]"));
        botaoAcessar.click();

        if (navegador.equalsIgnoreCase("firefox")) {
        	acceptSecurityAlert();
        }

        fecharPopup();

        this.janelaPrincipal = driver.getWindowHandle();
	}

	public void acessarPaginaTriagem() throws Exception {
        esperarCarregamento(500, 5, 1, "//p[contains(text(), 'Carregando')]");

        // navega diretamente para a página de triagem
        driver.get("http://spunet.planejamento.gov.br/#/servicos/triagem");
        esperarCarregamento(500, 5, 1, "//p[contains(text(), 'Carregando')]");
	}

	public void responderDemanda(String numeroAtendimento, String resposta, String complemento, File arquivo) throws Exception {
        pesquisarDemanda(numeroAtendimento); 

        List<WebElement> linhasRetornadas = encontrarElementos(5, 1, By.xpath("//table/tbody/tr"));
        String validacao = validarDemandaParaResponder(linhasRetornadas);
        if (validacao == null) {
        	abrirDemandaParaResponder(linhasRetornadas.iterator().next());
            preencherRespostaDemanda(resposta, complemento);
            realizarUploadArquivoResposta(arquivo);
            fecharDemanda();

            esperarCarregamento(2000, 5, 1, "//p[contains(text(), 'Carregando')]");
        } else {
        	throw new MyException(validacao);
        }
	}

	private String validarDemandaParaResponder(List<WebElement> linhasRetornadas) {
		if (linhasRetornadas.size() == 1) {
			return "Foram retornadas " + linhasRetornadas.size() + " linhas ao pesquisar. Não será possível responder automaticamente.";
		}
		
    	WebElement txtSituacao = linhasRetornadas.iterator().next().findElement(By.xpath("./td[6]"));
    	if (!txtSituacao.getText().trim().equalsIgnoreCase("Em Análise Técnica")) {
    		return "A solicitação não está em análise técnica e não pode ser respondida automaticamente";
    	}

    	return null;
	}

	private void fecharDemanda() {
		// botao para fechar a janela após clicar em enviar
		WebElement btnFechar = encontrarElemento(5, 1, By.xpath("//button[@ng-click='fechar()' and ./label[text() = 'fechar']]"));
		moverMouseParaElemento(btnFechar);
		btnFechar.click();
	}

	private void realizarUploadArquivoResposta(File arquivo) throws InterruptedException, Exception {
		WebElement txtUpload = encontrarElemento(5, 1, By.xpath("//input[@type = 'file']"));
		
		TimeUnit.MILLISECONDS.sleep(500);

		executarJavaScript("arguments[0].style.visibility = 'visible'; arguments[0].style.overflow = 'visible'; arguments[0].style.height = '1px'; arguments[0].style.width = '1px'; arguments[0].style.opacity = 1", txtUpload);

		TimeUnit.MILLISECONDS.sleep(500);
		
		txtUpload.sendKeys(arquivo.getAbsolutePath());
		
		esperarCarregamento(2000, 5, 1, "//p[contains(text(), 'Carregando')]");

		// busca o botão de enviar para clicar
		WebElement btnEnviar = encontrarElemento(5, 1, By.xpath("//button[text() = 'Enviar']"));
		moverMouseParaElemento(btnEnviar);
		btnEnviar.click();

		esperarCarregamento(2000, 15, 1, "//p[contains(text(), 'Carregando')]");
	}

	private void preencherRespostaDemanda(String resposta, String complemento) {
		WebElement optResposta = encontrarElemento(5, 1, By.xpath("//md-radio-button[@aria-label = '" + resposta + "']"));
		optResposta.click();
		
		if (!MyUtils.emptyStringIfNull(complemento).equals("")) {
		    WebElement txtInformacoesComplementares = encontrarElemento(5, 1, By.xpath("//textarea[@ng-model = 'analiseRequerimento.justificativa']"));
		    txtInformacoesComplementares.sendKeys(MyUtils.emptyStringIfNull(complemento));
		}
	}

	private void abrirDemandaParaResponder(WebElement linha) throws InterruptedException, Exception {
		WebElement btnExpandirOpcoes = linha.findElement(By.xpath("//md-fab-trigger"));
		executarJavaScript("arguments[0].click();", btnExpandirOpcoes);
		TimeUnit.MILLISECONDS.sleep(500);
		
		WebElement btnDetalhar = linha.findElement(By.xpath("//a[@ng-click = 'irParaDetalhar(item);']"));
		executarJavaScript("arguments[0].click();", btnDetalhar);
		TimeUnit.MILLISECONDS.sleep(500);
		
		esperarCarregamento(500, 5, 1, "//p[contains(text(), 'Carregando')]");
	}

	private void pesquisarDemanda(String numeroAtendimento) throws Exception {
		WebElement txtNumeroAtendimento = encontrarElemento(15, 1, By.xpath("//input[@ng-model = 'filtro.nuAtendimento']"));
        txtNumeroAtendimento.clear();
        txtNumeroAtendimento.sendKeys(numeroAtendimento);

        WebElement btnPesquisar = encontrarElemento(15, 1, By.xpath("//button[@aria-label = 'Pesquisar']"));
        btnPesquisar.click();

        esperarCarregamento(500, 5, 1, "//p[contains(text(), 'Carregando')]");
	}
}
