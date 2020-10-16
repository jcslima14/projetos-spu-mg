package framework.services;

import java.time.Duration;
import java.util.LinkedHashMap;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import framework.utils.MyUtils;

public class SeleniumService {
	@SuppressWarnings("serial")
	public WebDriver obterWebDriver(String navegador, boolean exibirNavegador, String pastaDeDownload) throws Exception {
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
		
		return driver;
	}
	
	public Wait<WebDriver> obterWait(WebDriver driver, int timeout, int pollingEvery) {
        return new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(timeout))
        		.pollingEvery(Duration.ofSeconds(pollingEvery))
        		.ignoring(NoSuchElementException.class);
	}
}
