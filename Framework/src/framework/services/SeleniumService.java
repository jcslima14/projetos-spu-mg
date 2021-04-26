package framework.services;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.WebDriverWait;

import framework.utils.MyUtils;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;

public class SeleniumService {
	
	protected WebDriver driver;
	protected String janelaPrincipal;
	protected String janelaAtual;
	protected String navegador;

	public SeleniumService(String navegador, String endereco, boolean exibirNavegador, String pastaDeDownload, int timeoutImplicito) throws Exception {
		this.navegador = navegador;
		this.driver = obterWebDriver(navegador, exibirNavegador, pastaDeDownload, timeoutImplicito);
		
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

		WebDriver driver = null;
		if (navegador.equalsIgnoreCase("chrome")) {
			ChromeOptions opcoes = new ChromeOptions();
			opcoes.addArguments("start-maximized"); // open Browser in maximized mode
			opcoes.addArguments("disable-infobars"); // disabling infobars
			opcoes.addArguments("--disable-extensions"); // disabling extensions
			opcoes.addArguments("--disable-gpu"); // applicable to windows os only
			opcoes.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
			opcoes.addArguments("--no-sandbox"); // Bypass OS security model
			
			opcoes.addArguments("--ignore-ssl-errors=yes");
			opcoes.addArguments("--allow-running-insecure-content");

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
			WebDriverManager.getInstance(DriverManagerType.CHROME).setup();
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
			opcoes.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);

			if (!exibirNavegador) {
				opcoes.setHeadless(true);
			}
			WebDriverManager.getInstance(DriverManagerType.FIREFOX).setup();
			driver = new FirefoxDriver(opcoes);
		}
		
        // driver.manage().timeouts().implicitlyWait(timeoutImplicito, TimeUnit.MINUTES);

		return driver;
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

	protected WebDriverWait espera(int timeout, int pollingEvery) {
		return new WebDriverWait(driver, timeout, pollingEvery * 1000);
	}

	public WebElement encontrarElemento(int timeout, int pollingEvery, By by) {
		return espera(timeout, pollingEvery).until(new Function<WebDriver, WebElement>() {
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

	public WebElement encontrarElemento(By by) {
		return encontrarElemento(60, 3, by);
	}

	public List<WebElement> encontrarElementos(int timeout, int pollingEvery, By by) {
		return espera(timeout, pollingEvery).until(new Function<WebDriver, List<WebElement>>() {
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

	public List<WebElement> encontrarElementos(By by) {
		return encontrarElementos(60, 3, by);
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

	protected void aguardarCargaListaDocumentos(String xpath, int quantRegistrosEsperados) throws Exception {
		aguardarCargaListaDocumentos(60, 3, xpath, quantRegistrosEsperados);
	}

	protected void aguardarCargaListaDocumentos(int timeout, int pollingEvery, String xpath, int quantRegistrosEsperados) throws InterruptedException {
		// encontra a quantidade de registros aptos a serem impressos
		do {
			List<WebElement> linhasAptas = encontrarElementos(timeout, pollingEvery, By.xpath(xpath));
			if (linhasAptas != null && linhasAptas.size() == quantRegistrosEsperados) {
				break;
			} else {
				TimeUnit.SECONDS.sleep(1);
			}
		} while (true);
	}

	protected void moverMouseParaElemento(WebElement e) {
		Actions acoes = new Actions(driver);
		try {
			acoes.moveToElement(e).build().perform();
		} catch (Exception ex) {
		}
	}

	protected void moverMouseParaElementoEClicar(WebElement e) {
		Actions acoes = new Actions(driver);
		try {
			acoes.moveToElement(e).click().build().perform();
		} catch (Exception ex) {
		}
	}

	protected void moverMouseParaElementoEClicarBotaoDireito(WebElement e) {
		Actions acoes = new Actions(driver);
		try {
			acoes.moveToElement(e).contextClick(e).build().perform();
		} catch (Exception ex) {
		}
	}
	
	protected void mudaFocoParaPopup(int janelasAbertas) throws Exception {
		janelaAtual = driver.getWindowHandle();
		do {
			if (driver.getWindowHandles().size() > janelasAbertas) {
				break;
			}
			TimeUnit.SECONDS.sleep(1);
		} while (true);
		
		for (String janelaAberta : driver.getWindowHandles()) {
			driver.switchTo().window(janelaAberta);
		}
	}

	public void esperarCarregamento(int esperaInicialEmMilissegundos, int timeout, int pollingEvery, String xpath) throws Exception {
        TimeUnit.MILLISECONDS.sleep(esperaInicialEmMilissegundos);

        WebElement infCarregando = null;
        do {
        	try {
        		infCarregando = encontrarElemento(timeout, pollingEvery, By.xpath(xpath));
        	} catch (Exception e) {
        		infCarregando = null;
        	}
        	try {
	        	if (infCarregando == null || !infCarregando.isDisplayed()) {
	        		break;
	        	}
        	} catch (StaleElementReferenceException e) {
        		break;
        	}
        } while (true);
	}

	public Alert obterAlerta(int timeout, int pollingEvery) {
	    WebDriverWait wait = new WebDriverWait(driver, timeout, pollingEvery * 1000);
	    Alert alert = null;
        try {
		    alert = wait.until(new Function<WebDriver, Alert>() {
		        public Alert apply(WebDriver driver) {
	                return driver.switchTo().alert();
		        }
		    });  
        } catch(NoAlertPresentException | TimeoutException e) {
        	return null;
        }

	    return alert;
	}

	protected boolean alertaPresente( ) {
		try { 
	        driver.switchTo().alert(); 
	        return true; 
	    } catch (NoAlertPresentException Ex) { 
	        return false; 
	    }
	}

	public void fecharJanelaAtual() {
		driver.close();
		driver.switchTo().window(janelaAtual);
	}
	
	public void atualizarPagina() {
    	driver.navigate().refresh();
	}
	
	public void acceptSecurityAlert() {
	    Alert alert = espera(30, 3).until(new Function<WebDriver, Alert>() {       

	        public Alert apply(WebDriver driver) {
	            try {
	                return driver.switchTo().alert();
	            } catch(NoAlertPresentException e) {
	                return null;
	            }
	        }  
	    });

	    alert.accept();
	}
	
	public int obterQuantidadeRegistrosEsperados(String texto, String[] regexes) {
		int qtd = 0;
		Pattern pattern = null;
		Matcher matcher = null;
		for (String regex : regexes) {
			pattern = Pattern.compile(regex);
			matcher = pattern.matcher(texto);
			if (matcher.find()) {
				if (matcher.groupCount() == 2) {
					qtd = Integer.parseInt(matcher.group(2)) - Integer.parseInt(matcher.group(1)) + 1;
					break;
				} else {
					qtd = Integer.parseInt(matcher.group(1));
					break;
				}
			}
		}
		return qtd;
	}
}
