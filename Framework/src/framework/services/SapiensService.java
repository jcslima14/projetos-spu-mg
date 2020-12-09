package framework.services;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

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

	public String[] obterInformacoesProcesso(WebElement linha) {
		List<WebElement> links = linha.findElements(By.xpath(".//a"));
		String nup = links.get(0).getText();
		String processoJudicial = null;
		if (links.size() > 1) {
			processoJudicial = links.get(1).getText().split("\\(")[0].trim();
		}

		String especie = linha.findElement(By.xpath("./td[4]/div")).getText().trim();
		String dataHora = linha.findElement(By.xpath("./td[7]/div")).getText();

		return new String[] { nup, processoJudicial, especie, dataHora };
	}

	public void baixarProcesso(WebElement linha) {
		// TODO Auto-generated method stub
		
	}
}
