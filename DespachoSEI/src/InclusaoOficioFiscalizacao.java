import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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

import framework.MyUtils;
import framework.SpringUtilities;
import model.Parametro;

@SuppressWarnings("serial")
public class InclusaoOficioFiscalizacao extends JInternalFrame {

	private JFileChooser filArquivo = new JFileChooser();
	private JButton btnAbrirArquivo = new JButton("Selecionar arquivo");
	private JLabel lblNomeArquivo = new JLabel("C:\\Users\\90768116600\\Documents\\planilha_oficios.xlsx") {{ setVerticalTextPosition(SwingConstants.TOP); setSize(600, 20); }};
	private JLabel lblArquivo = new JLabel("Arquivo:", JLabel.TRAILING) {{ setLabelFor(filArquivo); }};
	private JTextField txtNumeroProcesso = new JTextField("10154.138675/2019-49", 15);
	private JLabel lblNumeroProcesso = new JLabel("Nº Processo SEI:") {{ setLabelFor(txtNumeroProcesso); }};
	private JTextField txtNumeroDocumentoModelo = new JTextField("11047332", 15);
	private JLabel lblNumeroDocumentoModelo = new JLabel("Nº Documento Modelo:") {{ setLabelFor(txtNumeroDocumentoModelo); }};
	private JTextField txtBlocoAssinatura = new JTextField("157322", 15);
	private JLabel lblBlocoAssinatura = new JLabel("Nº Bloco Assinatura:") {{ setLabelFor(txtBlocoAssinatura); }};
	private JTextField txtUsuario = new JTextField("julio.lima", 15);
	private JLabel lblUsuario = new JLabel("Usuário:") {{ setLabelFor(txtUsuario); }};
	private JPasswordField txtSenha = new JPasswordField("astpuf.00", 15);
	private JLabel lblSenha = new JLabel("Senha:") {{ setLabelFor(txtSenha); }};
	private JPanel painelDados = new JPanel() {{ setLayout(new SpringLayout()); }};
	private JButton btnProcessar = new JButton("Processar"); 
	private JTextArea logArea = new JTextArea(30, 100);
	private JScrollPane areaDeRolagem = new JScrollPane(logArea);
	private DespachoServico despachoServico;

	public InclusaoOficioFiscalizacao(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		despachoServico = new DespachoServico(conexao);
		painelDados.add(new JPanel() {{ add(lblArquivo); add(btnAbrirArquivo); }});
		painelDados.add(lblNomeArquivo);
		painelDados.add(lblNumeroProcesso);
		painelDados.add(txtNumeroProcesso);
		painelDados.add(lblNumeroDocumentoModelo);
		painelDados.add(txtNumeroDocumentoModelo);
		painelDados.add(lblBlocoAssinatura);
		painelDados.add(txtBlocoAssinatura);
		painelDados.add(lblUsuario);
		painelDados.add(txtUsuario);
		painelDados.add(lblSenha);
		painelDados.add(txtSenha);
		painelDados.add(btnProcessar); 
		painelDados.add(new JPanel()); 

		SpringUtilities.makeGrid(painelDados,
	            7, 2, //rows, cols
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
								gerarOficios(txtUsuario.getText(), new String(txtSenha.getPassword()), txtNumeroProcesso.getText(), txtNumeroDocumentoModelo.getText(), txtBlocoAssinatura.getText());
							} catch (Exception e) {
								JOptionPane.showMessageDialog(null, "Erro ao gerar as respostas no SEI: \n \n" + e.getMessage());
								MyUtils.appendLogArea(logArea, "Erro ao gerar as respostas no SEI: \n \n" + e.getMessage() + "\n" + stackTraceToString(e));
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

		btnAbrirArquivo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Action detalhes = filArquivo.getActionMap().get("viewTypeDetails");
				detalhes.actionPerformed(null);
				int retorno = filArquivo.showOpenDialog(InclusaoOficioFiscalizacao.this);
				if (retorno == JFileChooser.APPROVE_OPTION) {
					if (filArquivo.getSelectedFile().exists()) {
						lblNomeArquivo.setText(filArquivo.getSelectedFile().getAbsolutePath());
					}
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

	private void gerarOficios(String usuario, String senha, String numeroProcesso, String numeroDocumentoModelo, String blocoAssinatura) throws Exception {
		MyUtils.appendLogArea(logArea, "Iniciando o navegador web...");
		System.setProperty("webdriver.chrome.driver", MyUtils.chromeWebDriverPath());
		ChromeOptions opcoes = new ChromeOptions();

		opcoes.addArguments("start-maximized"); // open Browser in maximized mode
		opcoes.addArguments("disable-infobars"); // disabling infobars
		opcoes.addArguments("--disable-extensions"); // disabling extensions
		opcoes.addArguments("--disable-gpu"); // applicable to windows os only
		opcoes.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
		opcoes.addArguments("--no-sandbox"); // Bypass OS security model

		opcoes.addArguments("--ignore-certificate-errors");
        WebDriver driver = new ChromeDriver(opcoes);
		
        // And now use this to visit Google
        driver.get(despachoServico.obterConteudoParametro(Parametro.ENDERECO_SEI));

        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(60))
        		.pollingEvery(Duration.ofSeconds(3))
        		.ignoring(NoSuchElementException.class);

        // este wait não deve ter seu tempo alterado, pois é usado apenas para buscar a variante <autor> no frame correto; se for aumentada, pode ter impacto negativo em função da quantidade de frames que pode conter um documento
        Wait<WebDriver> wait2 = new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(2))
        		.pollingEvery(Duration.ofSeconds(1))
        		.ignoring(NoSuchElementException.class);

        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Find the text input element by its name
        WebElement weUsuario = driver.findElement(By.id("txtUsuario"));
        weUsuario.sendKeys(usuario);

        // Find the text input element by its name
        WebElement weSenha = driver.findElement(By.id("pwdSenha"));
        weSenha.sendKeys(senha);

        // selecionar a unidade do SEI
        Select cbxOrgao = new Select(MyUtils.encontrarElemento(wait, By.id("selOrgao")));
        cbxOrgao.selectByVisibleText(despachoServico.obterConteudoParametro(Parametro.ORGAO_LOGIN_SEI));

        TimeUnit.MILLISECONDS.sleep(1500);

        // Find the text input element by its name
        WebElement botaoAcessar = driver.findElement(By.id("sbmLogin"));
        botaoAcessar.click();

        // verifica se foi aberto popup indesejado (fechar o popup)
        MyUtils.fecharPopup(driver);
        String janelaPrincipal = driver.getWindowHandle();

		// pesquisa o processo onde deverão ser incluídos os ofícios
		WebElement txtPesquisaRapida = MyUtils.encontrarElemento(wait, By.xpath("//input[@id = 'txtPesquisaRapida']"));
		txtPesquisaRapida.sendKeys(numeroProcesso);
		txtPesquisaRapida.sendKeys(Keys.RETURN);

		Map<String, Oficio> oficiosAGerar = obterDadosOficios(lblNomeArquivo.getText());
		for (Oficio oficio : oficiosAGerar.values()) {
			MyUtils.appendLogArea(logArea, "Gerando ofício para UG: " + oficio.ugResponsavel);

			// clica no número do processo para habilitar os botões de ação do processo
			driver.switchTo().frame("ifrArvore");
			WebElement lnkNumeroProcesso = MyUtils.encontrarElemento(wait, By.xpath("//a/span[contains(text(), '" + numeroProcesso + "')]"));
			lnkNumeroProcesso.click();
			driver.switchTo().defaultContent();
			
			// clica em inserir documento
			driver.switchTo().frame("ifrVisualizacao");
			WebElement btnIncluirDocumento = MyUtils.encontrarElemento(wait, By.xpath("//img[@alt = 'Incluir Documento']"));
			btnIncluirDocumento.click();

			// clica no tipo de documento
			WebElement btnOpcaoTipoDocumento = MyUtils.encontrarElemento(wait, By.xpath("//a[text() = 'Ofício']"));
			btnOpcaoTipoDocumento.click();

			// clica em documento modelo
			WebElement lblDocumentoModelo = MyUtils.encontrarElemento(wait, By.xpath("//label[contains(text(), 'Documento Modelo')]"));
			lblDocumentoModelo.click();

			// preenche o código do documento modelo
			WebElement txtCodigoDocumentoModelo = MyUtils.encontrarElemento(wait, By.xpath("//input[@id = 'txtProtocoloDocumentoTextoBase']"));
			txtCodigoDocumentoModelo.sendKeys(numeroDocumentoModelo);

			// seleciona nivel de acesso do documento
			WebElement lblNivelAcessoPublico = MyUtils.encontrarElemento(wait, By.xpath("//label[@id = 'lblPublico']"));
			lblNivelAcessoPublico.click();
			
			// clica em confirmar dados
			WebElement btnConfirmarDados = MyUtils.encontrarElemento(wait, By.xpath("//button[@id = 'btnSalvar']"));
			btnConfirmarDados.click();

			// esperar abrir a janela popup
			do {
				TimeUnit.SECONDS.sleep(1);
			} while (driver.getWindowHandles().size() == 1);
			
			// abriu janela para editar o documento, então navega até a janela
			for (String tituloJanela : driver.getWindowHandles()) {
				driver.switchTo().window(tituloJanela);
			}

			String numeroDocumentoSEIGerado = driver.getTitle().split(" - ")[1];
			MyUtils.appendLogArea(logArea, "Nº Documento Gerado: " + numeroDocumentoSEIGerado);

			// alterna para o frame de destinatário para substituir os dados e clica no primeiro elemento p para mudar o foco
			TimeUnit.SECONDS.sleep(1);
			driver.switchTo().frame(3);
			MyUtils.encontrarElemento(wait, By.xpath("(//p)[1]")).click();
			TimeUnit.SECONDS.sleep(1);

			substituirMarcacaoDocumento(driver, wait, oficio.mapaSubstituicoesCabecalho());

			// alterna para o frame do corpo do documento para promover as substituições e clica no primeiro elemento p para mudar o foco
			driver.switchTo().frame(4);
			MyUtils.encontrarElemento(wait, By.xpath("(//p)[1]")).click();
			TimeUnit.SECONDS.sleep(1);
			
			// obtem a linha da tabela que possui os marcadores a serem substituídos e armazena em string para servir de template para novas linhas a serem adicionadas
			String imovelTemplate = MyUtils.encontrarElemento(wait, By.xpath("//table[@id = 'tabela-imoveis-vistoria']/tbody/tr[./td]")).getAttribute("outerHTML");

			// exclui a linha de template para, em seguida, adicionar as linhas com os dados reais
			js.executeScript("document.querySelector('#tabela-imoveis-vistoria > tbody').removeChild(document.querySelector('#tabela-imoveis-vistoria > tbody').lastChild)");
			TimeUnit.MILLISECONDS.sleep(500);
			
			String novosImoveis = "";
			
			// adicionar linhas à tabela de imóveis a vistoriar
			for (Imovel imovel : oficio.listaImoveis) {
				novosImoveis += imovel.substituirMarcadores(imovelTemplate);
				TimeUnit.MILLISECONDS.sleep(500);
			}

			// adiciona os novos imóveis à tabela
			js.executeScript("document.querySelector('#tabela-imoveis-vistoria > tbody').innerHTML += '" + novosImoveis + "'");
			TimeUnit.MILLISECONDS.sleep(500);

			// substitui os marcadores do corpo
			substituirMarcacaoDocumento(driver, wait, oficio.mapaSubstituicoesCorpo());
		
			// procura o botão salvar, conferindo que ele esteja habilitado
			WebElement btnSalvar = MyUtils.encontrarElemento(wait, By.xpath("//div[contains(@id, 'cke_txaEditor') and contains(@class, 'cke_detached') and not(contains(@style, 'display: none'))]//a[contains(@title, 'Salvar') and not(@aria-disabled)]"));
			btnSalvar.click();
				
			TimeUnit.MILLISECONDS.sleep(500);
			
			// aguarda até que o botão de salvar esteja novamente desabilitado para fechar a janela
			MyUtils.encontrarElemento(wait, By.xpath("//div[contains(@id, 'cke_txaEditor') and contains(@class, 'cke_detached') and not(contains(@style, 'display: none'))]//a[contains(@title, 'Salvar') and @aria-disabled]"));
			
			driver.close();
			driver.switchTo().window(janelaPrincipal);

			driver.switchTo().frame(MyUtils.encontrarElemento(wait, By.id("ifrVisualizacao")));
			WebElement btnIncluirBlocoAssinatura = MyUtils.encontrarElemento(wait, By.xpath("//img[@alt = 'Incluir em Bloco de Assinatura']"));
			btnIncluirBlocoAssinatura.click();

			// seleciona o bloco interno desejado
			Select cbxBlocoAssinatura = new Select(MyUtils.encontrarElemento(wait, By.id("selBloco")));
			cbxBlocoAssinatura.selectByValue(blocoAssinatura);

			// aguardar que a linha com o documento gerado seja carregada
			MyUtils.encontrarElemento(wait, By.xpath("//table[@id = 'tblDocumentos']/tbody/tr[./td[2]/a[text() = '" + numeroDocumentoSEIGerado + "']]"));

			// clica em incluir
			WebElement btnIncluir = MyUtils.encontrarElemento(wait, By.id("sbmIncluir"));
			btnIncluir.click();

			// aguardar que a linha retorno indicando que o registro está inserido no bloco
			MyUtils.encontrarElemento(wait, By.xpath("//table[@id = 'tblDocumentos']/tbody/tr[./td[2]/a[text() = '" + numeroDocumentoSEIGerado + "'] and ./td[5]/a[text() = '" + blocoAssinatura + "']]"));
			
			driver.switchTo().defaultContent();
		} // fim do loop de todas as respostas a gerar

		MyUtils.appendLogArea(logArea, "Fim do Processamento...");

        driver.close();
        driver.quit();
	}

	private class Imovel {
		String municipioImovel;
		String ripImovel;
		String ripUtilizacao;
		String regimeUtilizacao;
		String enderecoImovel;
		String areaImovel;

		public Imovel(String municipioImovel, String ripImovel, String ripUtilizacao, String regimeUtilizacao, String enderecoImovel, String areaImovel) {
			this.municipioImovel = municipioImovel;
			this.ripImovel = ripImovel;
			this.ripUtilizacao = ripUtilizacao;
			this.regimeUtilizacao = regimeUtilizacao;
			this.enderecoImovel = enderecoImovel;
			this.areaImovel = areaImovel;
		}

		public String substituirMarcadores(String imovelTemplate) {
			return imovelTemplate
					.replaceAll("@municipio_imovel@", this.municipioImovel)
					.replaceAll("@rip_imovel@", this.ripImovel)
					.replaceAll("@rip_utilizacao@", this.ripUtilizacao)
					.replaceAll("@regime_utilizacao@", this.regimeUtilizacao)
					.replaceAll("@endereco_imovel@", this.enderecoImovel)
					.replaceAll("@area_imovel@", this.areaImovel);
		}
	}
	
	private class Oficio {
		String ugResponsavel;
		String vocativoDestinatario;
		String tratamentoDestinatario;
		String nomeDestinatario;
		String cargoDestinatario;
		String pessoaJuridica;
		String enderecoDestinatario;
		String complementoEnderecoDestinatario;
		String emailDestinatario;
		List<Imovel> listaImoveis = new ArrayList<Imovel>();

		public Oficio(String ugResponsavel, String vocativoDestinatario, String tratamentoDestinatario, String nomeDestinatario, String cargoDestinatario, String pessoaJuridica, String enderecoDestinatario, String complementoEnderecoDestinatario, String emailDestinatario) {
			this.ugResponsavel = ugResponsavel;
			this.vocativoDestinatario = vocativoDestinatario;
			this.tratamentoDestinatario = tratamentoDestinatario;
			this.nomeDestinatario = nomeDestinatario;
			this.cargoDestinatario = cargoDestinatario;
			this.pessoaJuridica = pessoaJuridica;
			this.enderecoDestinatario = enderecoDestinatario;
			this.complementoEnderecoDestinatario = complementoEnderecoDestinatario;
			this.emailDestinatario = emailDestinatario;
		}

		public Map<String, String> mapaSubstituicoesCabecalho() {
			Map<String, String> mapaSubstituicoes = new LinkedHashMap<String, String>();
			
			mapaSubstituicoes.put("@tratamento_destinatario@", this.tratamentoDestinatario);
			mapaSubstituicoes.put("@nome_destinatario@", this.nomeDestinatario);
			mapaSubstituicoes.put("@cargo_destinatario@", this.cargoDestinatario);
			mapaSubstituicoes.put("@pessoa_juridica_destinatario@", this.pessoaJuridica);
			mapaSubstituicoes.put("@endereco_destinatario@", this.enderecoDestinatario);
			mapaSubstituicoes.put("@complemento_endereco_destinatario@", this.complementoEnderecoDestinatario);
			mapaSubstituicoes.put("@email_destinatario@", this.emailDestinatario);

			return mapaSubstituicoes;
		}

		public Map<String, String> mapaSubstituicoesCorpo() {
			Map<String, String> mapaSubstituicoes = new LinkedHashMap<String, String>();
			
			mapaSubstituicoes.put("@vocativo_destinatario@", this.vocativoDestinatario);
			mapaSubstituicoes.put("@ug_responsavel@", this.ugResponsavel);

			return mapaSubstituicoes;
		}

		private void adicionaImovel(String municipio, String ripImovel, String ripUtilizacao, String regimeUtiliacao, String enderecoImovel, String areaImovel) {
			Imovel imovel = new Imovel(municipio, ripImovel, ripUtilizacao, regimeUtiliacao, enderecoImovel, areaImovel);
			listaImoveis.add(imovel);
		}
	}

	private Map<String, Oficio> obterDadosOficios(String arquivo) throws Exception {
		File fileInput = new File(arquivo);
		Workbook wb = WorkbookFactory.create(fileInput);
		Sheet planilha = wb.getSheetAt(0);
		Map<String, Oficio> retorno = new LinkedHashMap<String, Oficio>();
		String chaveAnterior = "";

		for (int l = 1; l <= planilha.getLastRowNum(); l++) {
			Row linha = planilha.getRow(l);
			String ugResponsavel = MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(0))).trim();
			String chaveAtual = ugResponsavel.toLowerCase();
			// caso a chave lida seja em branco, considera que é igual à ultima chave lida (conceito mestre-detalhe para UG Responsável-Imóvel)
			if (chaveAtual.equals("")) chaveAtual = chaveAnterior;
			
			// obtem o ofício do mapa
			Oficio oficio = retorno.get(chaveAtual);
			
			// se o ofício lido não foi encontrado, gera um novo ofício, preenchendo seus dados
			if (oficio == null) {
				oficio = new Oficio(
						ugResponsavel, 
						MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(1))).trim(),
						MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(2))).trim(),
						MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(3))).trim(),
						MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(4))).trim(),
						MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(5))).trim(),
						MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(6))).trim(),
						MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(7))).trim(),
						MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(8))).trim()
						);
				
				retorno.put(chaveAtual, oficio);
			}
			
			oficio.adicionaImovel(
					MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(9))).trim(), 
					MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(10))).trim(),
					MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(11))).trim(),
					MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(12))).trim(),
					MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(13))).trim(),
					MyUtils.formatarNumero(Double.parseDouble(MyUtils.obterValorCelula(linha.getCell(14))), "#,##0.00").trim()
					);
			MyUtils.appendLogArea(logArea, "Lendo a linha " + (l+1) + "/" + (planilha.getLastRowNum()+1) + "...");
			chaveAnterior = chaveAtual;
		}
		MyUtils.appendLogArea(logArea, "Fim de leitura da planilha!");
		wb.close();

		return retorno;
	}

	private void substituirMarcacaoDocumento(WebDriver driver, Wait<WebDriver> wait, Map<String, String> mapaSubstituicoes) throws Exception {
		// volta ao conteúdo default
		driver.switchTo().defaultContent();

		// clica no botão localizar
		WebElement btnSubstituir = MyUtils.encontrarElemento(wait, By.xpath("//div[contains(@id, 'cke_txaEditor') and contains(@class, 'cke_detached') and not(contains(@style, 'display: none'))]//a[@title = 'Substituir']"));
		btnSubstituir.click();
		TimeUnit.MILLISECONDS.sleep(500);

		// repetir este pedaço para todos os textos a serem substituídos no documento
		for (String chave : mapaSubstituicoes.keySet()) {
			String textoSubstituto = mapaSubstituicoes.get(chave);

			// preenche o texto a ser encontrado
			WebElement txtPesquisar = MyUtils.encontrarElemento(wait, By.xpath("(//div[@role= 'dialog' and not(contains(@style, 'display: none'))]//div[@role = 'tabpanel' and not(contains(@style, 'display: none'))]//input[@type = 'text'])[1]"));
			txtPesquisar.clear();
			txtPesquisar.sendKeys(chave);
			
			// preenche o texto para substituição
			WebElement txtSubstituir = MyUtils.encontrarElemento(wait, By.xpath("(//div[@role= 'dialog' and not(contains(@style, 'display: none'))]//div[@role = 'tabpanel' and not(contains(@style, 'display: none'))]//input[@type = 'text'])[2]"));
			txtSubstituir.clear();
			txtSubstituir.sendKeys(textoSubstituto);
			
			// clica em substituir tudo
			WebElement btnSubstituirTudo = MyUtils.encontrarElemento(wait, By.xpath("//div[@role= 'dialog' and not(contains(@style, 'display: none'))]//a[@title = 'Substituir Tudo']"));
			btnSubstituirTudo.click();
			
			// clica em ok na mensagem apresentada
			driver.switchTo().alert().accept();
		}
		
		// clica em fechar
		WebElement btnFechar = MyUtils.encontrarElemento(wait, By.xpath("//div[@role= 'dialog' and not(contains(@style, 'display: none'))]//span[text() = 'Fechar']"));
		btnFechar.click();
	}
}
