import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;

import com.sun.glass.events.KeyEvent;

@SuppressWarnings("serial")
public class ImpressaoDespachoSO_GUI extends JInternalFrame {

	private Connection conexao;
	private JTextField txtUsuario = new JTextField(15);
	private JLabel lblUsuario = new JLabel("Usuário:") {{ setLabelFor(txtUsuario); }};
	private JPasswordField txtSenha = new JPasswordField(15);
	private JLabel lblSenha = new JLabel("Senha:") {{ setLabelFor(txtSenha); }};
	private JPanel painelDados = new JPanel() {{ setLayout(new SpringLayout()); }};
	private JButton btnProcessar = new JButton("Processar"); 
	private JTextArea logArea = new JTextArea(30, 100);
	private JScrollPane areaDeRolagem = new JScrollPane(logArea);
	private DespachoServico despachoServico;

	public ImpressaoDespachoSO_GUI(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		this.conexao = conexao;
		despachoServico = new DespachoServico(conexao);

		painelDados.add(lblUsuario);
		painelDados.add(txtUsuario);
		painelDados.add(lblSenha);
		painelDados.add(txtSenha);
		painelDados.add(btnProcessar); 
		painelDados.add(new JPanel()); 
		
		SpringUtilities.makeGrid(painelDados,
	            3, 2, //rows, cols
	            6, 6, //initX, initY
	            6, 6); //xPad, yPad

		add(painelDados, BorderLayout.WEST);
		add(areaDeRolagem, BorderLayout.SOUTH);

		btnProcessar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					logArea.setText("");
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								imprimirDespachoSEI(txtUsuario.getText(), new String(txtSenha.getPassword()));;
							} catch (Exception e) {
								MyUtils.appendLogArea(logArea, "Erro ao imprimir os despachos do SEI: \n \n" + e.getMessage() + "\n" + stackTraceToString(e));
								e.printStackTrace();
							}
						}

						private String stackTraceToString(Exception e) {
							StringWriter sw = new StringWriter();
							e.printStackTrace(new PrintWriter(sw));
							return sw.toString();
						}
					}).start();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void imprimirDespachoSEI(String usuario, String senha) throws Exception {
		MyUtils.appendLogArea(logArea, "Iniciando o navegador web...");
		ChromeOptions opcoes = new ChromeOptions();
		opcoes.setExperimentalOption("prefs", new LinkedHashMap<String, Object>() {{ put("download.prompt_for_download", true); }});
		System.setProperty("webdriver.chrome.driver", "./resources/chromedriver/chromedriver.exe");
        WebDriver driver = new ChromeDriver(opcoes);
    	Teclado teclado = new Teclado();

        // And now use this to visit Google
        driver.get(despachoServico.obterConteudoParametro(Parametro.ENDERECO_SEI));

        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(15))
        		.pollingEvery(Duration.ofSeconds(3))
        		.ignoring(NoSuchElementException.class);

        Wait<WebDriver> wait5 = new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(5))
        		.pollingEvery(Duration.ofSeconds(3))
        		.ignoring(NoSuchElementException.class);

        // Find the text input element by its name
        WebElement weUsuario = driver.findElement(By.id("txtUsuario"));
        weUsuario.sendKeys(usuario);

        // Find the text input element by its name
        WebElement weSenha = driver.findElement(By.id("pwdSenha"));
        weSenha.sendKeys(senha);

        // Find the text input element by its name
        WebElement botaoAcessar = driver.findElement(By.id("sbmLogin"));
        botaoAcessar.click();

        // verifica se foi aberto popup indesejado (fechar o popup)
        fecharPopup(driver);

		List<Despacho> despachosAImprimir = obterDespachosACadastrar();
		for (Despacho despachoAImprimir : despachosAImprimir) {
			String numeroProcesso = despachoAImprimir.getNumeroProcesso();
			String numeroDespacho = despachoAImprimir.getNumeroDocumentoSEI();
			String caminhoDespachos = despachoAImprimir.getDestino().getCaminhoDespachos();

			// processamento....
			MyUtils.appendLogArea(logArea, "Processo: " + numeroProcesso + " - Nº Despacho: " + numeroDespacho);

			driver.switchTo().defaultContent();

			// pesquisa o processo onde deverá ser incluído o despacho
			WebElement txtPesquisaRapida = MyUtils.encontrarElemento(wait, By.xpath("//input[@id = 'txtPesquisaRapida']"));
			txtPesquisaRapida.sendKeys(numeroDespacho);
			txtPesquisaRapida.sendKeys(Keys.RETURN);
			
			// procura o frame de visualização (se não retornar, indica que a pesquisa não resultou em um documento único)
			WebElement ifrVisualizacao = null;
			
			try {
				ifrVisualizacao = MyUtils.encontrarElemento(wait, By.id("ifrVisualizacao"));
			} catch (Exception e) {
				MyUtils.appendLogArea(logArea, "Documento não encontrado...");
			}
			
			if (ifrVisualizacao == null) {
				continue;
			}

			// verifica se o documento foi assinado por 2 pessoas
			driver.switchTo().frame(ifrVisualizacao);
			driver.switchTo().frame("ifrArvoreHtml");

//			MyUtils.encontrarElemento(wait, By.xpath("//p[contains(text(), 'Documento assinado eletronicamente por')]"));
			List<WebElement> assinaturas = MyUtils.encontrarElementos(wait5, By.xpath("//p[contains(text(), 'Documento assinado eletronicamente por')]"));

			if (assinaturas.size() != 2) {
				MyUtils.appendLogArea(logArea, "Para ser impresso, o documento precisa de 2 assinaturas. Este documento possui " + assinaturas.size() + " assinaturas.");
				continue;
			}

			driver.switchTo().defaultContent();
			driver.switchTo().frame(ifrVisualizacao);

			// clica em imprimir documento
			WebElement btnImprimirDocumento = MyUtils.encontrarElemento(wait, By.xpath("//img[@alt = 'Imprimir Web']"));
			btnImprimirDocumento.click();
			
			// ------------ início do processo de impressão
			TimeUnit.SECONDS.sleep(5);

			int count = 0;
			
			// abriu a janela de impressão, então navega até ela para realizar a impressão
			String janelaAnterior = driver.getWindowHandle();
			String janelaImpressao = "";

			for (String tituloJanela : driver.getWindowHandles()) {
				janelaImpressao = tituloJanela;
				count ++;
				if (count == 2) break;
			}

			driver.switchTo().window(janelaImpressao);

			WebElement printPreviewApp = driver.findElement(By.xpath("//print-preview-app"));
			WebElement printPreviewAppRoot = (WebElement) ((JavascriptExecutor) driver).executeScript("return arguments[0].shadowRoot", printPreviewApp);

			WebElement printPreviewSideBar = printPreviewAppRoot.findElement(By.cssSelector("print-preview-sidebar"));
			WebElement printPreviewSideBarRoot = (WebElement) ((JavascriptExecutor) driver).executeScript("return arguments[0].shadowRoot", printPreviewSideBar);

			WebElement printPreviewDestinationSettings = printPreviewSideBarRoot.findElement(By.cssSelector("print-preview-destination-settings"));
			WebElement printPreviewDestinationSettingsRoot = (WebElement) ((JavascriptExecutor) driver).executeScript("return arguments[0].shadowRoot", printPreviewDestinationSettings);

			WebElement printPreviewDestinationSelect = printPreviewDestinationSettingsRoot.findElement(By.cssSelector("print-preview-destination-select"));
			WebElement printPreviewDestinationSelectRoot = (WebElement) ((JavascriptExecutor) driver).executeScript("return arguments[0].shadowRoot", printPreviewDestinationSelect);

			Select destino = new Select(printPreviewDestinationSelectRoot.findElement(By.cssSelector("select.md-select")));
			destino.selectByValue("Save as PDF/local/");

			WebElement printPreviewHeader = printPreviewSideBarRoot.findElement(By.cssSelector("print-preview-header"));
			WebElement printPreviewHeaderRoot = (WebElement) ((JavascriptExecutor) driver).executeScript("return arguments[0].shadowRoot", printPreviewHeader);

			WebElement botaoSalvar = printPreviewHeaderRoot.findElement(By.cssSelector("paper-button.action-button"));

			botaoSalvar.click();

			String nomeArquivo = caminhoDespachos + "\\" + numeroProcesso + ".pdf";
			File arquivo = new File(nomeArquivo);
			if (arquivo.exists()) arquivo.delete();
			
			TimeUnit.SECONDS.sleep(2);

			// manipular a janela de salvar arquivo que se abre
			teclado.digitar(KeyEvent.VK_DELETE);
			teclado.digitar(nomeArquivo);
			teclado.digitar("\n");

			TimeUnit.SECONDS.sleep(2);
			driver.switchTo().window(janelaAnterior);
			
			// ------------ final do processo de impressão

			// verifica se o arquivo foi gerado e atualiza o número do despacho gerado no SEI
			if ((new File(nomeArquivo)).exists()) {
				atualizarDespachoGerado(despachoAImprimir);
			}
		}

		MyUtils.appendLogArea(logArea, "Fim do Processamento...");

        driver.close();
        driver.quit();
	}

	private void atualizarDespachoGerado(Despacho despacho) throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("update despacho"
				 + "   set despachoimpresso = true "
				 + "	 , datahoraimpressao = datetime('now', 'localtime') "
				 + " where despachoid = " + despacho.getDespachoId());

		MyUtils.execute(conexao, sql.toString());
	}

	private void fecharPopup(WebDriver driver) {
		String primeiraJanela = "";
        for (String tituloJanela : driver.getWindowHandles()) {
        	if (!primeiraJanela.equalsIgnoreCase("")) {
        		driver.switchTo().window(tituloJanela);
        		driver.close();
        	} else {
        		primeiraJanela = tituloJanela;
        	}
        }

        driver.switchTo().window(primeiraJanela);
	}

	private List<Despacho> obterDespachosACadastrar() throws Exception {
		List<Despacho> retorno = new ArrayList<Despacho>();

		StringBuilder sql = new StringBuilder("");
//		sql.append("select * "
//				 + "  from despacho dp "
//				 + " where numerodocumentosei <> '' "
//				 + "   and exists (select 1 from destino dt where dp.destinoid = dt.destinoid and caminhodespachos <> '') "
//				 + " order by numerodocumentosei desc limit 30 ");
		sql.append("select * "
				 + "  from despacho dp "
				 + " where coalesce(numerodocumentosei, '') <> '' "
				 + "   and not despachoimpresso "
				 + "   and coalesce(numeroprocessosei, '') <> '' "
				 + "   and exists (select 1 from destino dt where dp.destinoid = dt.destinoid and caminhodespachos <> '') ");
//				 + " limit 1 ");

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			retorno.add(new Despacho(rs.getInt("despachoid"),
									 rs.getString("datadespacho"),
									 despachoServico.obterTipoProcesso(rs.getInt("tipoprocessoid"), null).iterator().next(),
									 rs.getString("numeroprocesso"),
									 rs.getString("autor"),
									 rs.getString("comarca"),
									 despachoServico.obterTipoImovel(rs.getInt("tipoimovelid"), null).iterator().next(),
									 rs.getString("endereco"),
									 rs.getString("municipio"),
									 rs.getString("coordenada"),
									 rs.getString("area"),
									 despachoServico.obterTipoDespacho(rs.getInt("tipodespachoid"), null).iterator().next(),
									 despachoServico.obterAssinante(rs.getInt("assinanteid"), null, null).iterator().next(),
									 despachoServico.obterDestino(rs.getInt("destinoid"), null, null, null).iterator().next(),
									 rs.getString("observacao"),
									 rs.getString("numerodocumentosei"),
									 rs.getString("datahoradespacho"),
									 rs.getString("numeroprocessosei"),
									 rs.getBoolean("arquivosanexados"),
									 rs.getBoolean("despachoimpresso"),
									 rs.getString("datahoraimpressao"),
									 rs.getString("blocoassinatura"),
									 rs.getBoolean("despachonoblocoassinatura")
									 ));
		}
		
		return retorno;
	}
}
