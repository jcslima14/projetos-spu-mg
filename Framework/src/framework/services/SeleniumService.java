package framework.services;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import framework.utils.MyUtils;

public class SeleniumService {
	
	protected WebDriver driver;
	protected WebDriverWait wait;
	protected String janelaPrincipal;

	public SeleniumService(String navegador, String endereco, boolean exibirNavegador, String pastaDeDownload, int timeoutImplicito) throws Exception {
		this.driver = obterWebDriver(navegador, exibirNavegador, pastaDeDownload, timeoutImplicito);
		this.wait = new WebDriverWait(driver, timeoutImplicito * 60);
		this.wait.pollingEvery(Duration.ofSeconds(3));
		this.wait.withTimeout(Duration.ofSeconds(60));
		this.wait.ignoring(NoSuchElementException.class);
		
		if (!endereco.trim().equals("")) {
			acessarEndereco(endereco);
		}
	}
	
	@SuppressWarnings("serial")
	private WebDriver obterWebDriver(String navegador, boolean exibirNavegador, String pastaDeDownload, int timeoutImplicito) throws Exception {
		// verifica se a pasta de downloads existe
		if (!pastaDeDownload.contentEquals("") && !MyUtils.arquivoExiste(pastaDeDownload)) {
			throw new Exception("A pasta para download dos arquivos não existe");
		}

		System.setProperty("webdriver.chrome.driver", MyUtils.chromeWebDriverPath());
		WebDriver driver = null;
		if (navegador.equalsIgnoreCase("chrome")) {
			ChromeOptions opcoes = new ChromeOptions();
			opcoes.addArguments("start-maximized"); // open Browser in maximized mode
			opcoes.addArguments("disable-infobars"); // disabling infobars
			opcoes.addArguments("--disable-extensions"); // disabling extensions
			opcoes.addArguments("--disable-gpu"); // applicable to windows os only
			opcoes.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
			opcoes.addArguments("--no-sandbox"); // Bypass OS security model

			opcoes.addArguments("--ignore-certificate-errors");

			opcoes.setExperimentalOption("prefs", new LinkedHashMap<String, Object>() {{ 
				put("download.prompt_for_download", false); 
				put("download.default_directory", pastaDeDownload); 
				put("pdfjs.disabled", true); 
				put("plugins.always_open_pdf_externally", true);
				}});
			opcoes.addArguments("--disable-extensions");
			if (!exibirNavegador) {
				opcoes.setHeadless(true);
			}
			System.setProperty("webdriver.chrome.driver", MyUtils.chromeWebDriverPath());
	        driver = new ChromeDriver(opcoes);
		} else {
			FirefoxOptions opcoes = new FirefoxOptions();
			opcoes.addPreference("browser.download.folderList", 2);
			opcoes.addPreference("browser.download.dir", pastaDeDownload);
			opcoes.addPreference("browser.download.useDownloadDir", true);
			opcoes.addPreference("browser.helperApps.neverAsk.saveToDisk", "application/pdf");
			opcoes.addPreference("browser.link.open_newwindow", 3);
			opcoes.addPreference("pdfjs.disabled", true);  // disable the built-in PDF viewer
			opcoes.addPreference("pdfjs.previousHandler.alwaysAskBeforeHandling", true);
			opcoes.addPreference("pdfjs.previousHandler.preferredAction", 4);
			opcoes.addPreference("pdfjs.enabledCache.state", false);
			if (!exibirNavegador) {
				opcoes.setHeadless(true);
			}
			System.setProperty("webdriver.gecko.driver", MyUtils.firefoxWebDriverPath());
			driver = new FirefoxDriver(opcoes);
		}
		
        // driver.manage().timeouts().implicitlyWait(timeoutImplicito, TimeUnit.MINUTES);

		return driver;
	}
	
	public Wait<WebDriver> obterWait(WebDriver driver, int timeout, int pollingEvery) {
        return new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(timeout))
        		.pollingEvery(Duration.ofSeconds(pollingEvery))
        		.ignoring(NoSuchElementException.class);
	}
	
	public void acessarEndereco(String endereco) {
		driver.get(endereco);
	}

	public void fecharPopup() {
		String primeiraJanela = "";
        for (String tituloJanela : this.driver.getWindowHandles()) {
        	if (!primeiraJanela.equalsIgnoreCase("")) {
        		this.driver.switchTo().window(tituloJanela);
        		this.driver.close();
        	} else {
        		primeiraJanela = tituloJanela;
        	}
        }

        this.driver.switchTo().window(primeiraJanela);
	}

	public WebElement encontrarElemento(By by) {
		return this.wait.until(new Function<WebDriver, WebElement>() {
			@Override
			public WebElement apply(WebDriver t) {
				WebElement element = t.findElement(by);
				if (element == null) {
					System.out.println("Elemento não encontrado...");
				}
				return element;
			}
		});
	}

	public List<WebElement> encontrarElementos(By by) {
		return this.wait.until(new Function<WebDriver, List<WebElement>>() {
			@Override
			public List<WebElement> apply(WebDriver t) {
				List<WebElement> elements = t.findElements(by);
				if (elements == null) {
					System.out.println("Elemento não encontrado...");
				}
				return elements;
			}
		});
	}
	
	public void alterarParaFrame(int index) {
		driver.switchTo().frame(index);
	}

	public void executarJavaScript(String script, Object... args) {
		((JavascriptExecutor) driver).executeScript(script, args);
	}
	
	public void fechaNavegador() {
        driver.close();
        driver.quit();
	}
}
