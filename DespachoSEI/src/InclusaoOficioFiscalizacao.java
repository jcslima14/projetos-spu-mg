import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;

import framework.JPAUtils;
import framework.MyComboBox;
import framework.MyLabel;
import framework.MyUtils;
import framework.SpringUtilities;
import model.Assinante;
import model.AssinanteTipoResposta;
import model.Destino;
import model.Municipio;
import model.Origem;
import model.Parametro;
import model.Solicitacao;
import model.SolicitacaoEnvio;
import model.SolicitacaoResposta;
import model.TipoImovel;
import model.TipoProcesso;
import model.TipoResposta;

@SuppressWarnings("serial")
public class InclusaoOficioFiscalizacao extends JInternalFrame {

	private EntityManager conexao;
	private JFileChooser filArquivo = new JFileChooser();
	private JButton btnAbrirArquivo = new JButton("Selecionar arquivo");
	private JLabel lblNomeArquivo = new JLabel("") {{ setVerticalTextPosition(SwingConstants.TOP); setSize(600, 20); }};
	private JLabel lblArquivo = new JLabel("Arquivo:", JLabel.TRAILING) {{ setLabelFor(filArquivo); }};
	private JTextField txtNumeroProcesso = new JTextField(15);
	private JLabel lblNumeroProcesso = new JLabel("N� Processo SEI:") {{ setLabelFor(txtNumeroProcesso); }};
	private JTextField txtNumeroDocumentoModelo = new JTextField(15);
	private JLabel lblNumeroDocumentoModelo = new JLabel("N� Documento Modelo:") {{ setLabelFor(txtNumeroDocumentoModelo); }};
	private JTextField txtUsuario = new JTextField(15);
	private JLabel lblUsuario = new JLabel("Usu�rio:") {{ setLabelFor(txtUsuario); }};
	private JPasswordField txtSenha = new JPasswordField(15);
	private JLabel lblSenha = new JLabel("Senha:") {{ setLabelFor(txtSenha); }};
	private JPanel painelDados = new JPanel() {{ setLayout(new SpringLayout()); }};
	private JButton btnProcessar = new JButton("Processar"); 
	private JTextArea logArea = new JTextArea(30, 100);
	private JScrollPane areaDeRolagem = new JScrollPane(logArea);
	private DespachoServico despachoServico;
	private Assinante superior;

	public InclusaoOficioFiscalizacao(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		this.conexao = conexao;
		despachoServico = new DespachoServico(conexao);

		painelDados.add(new JPanel() {{ add(lblArquivo); add(btnAbrirArquivo); }});
		painelDados.add(lblNomeArquivo);
		painelDados.add(lblNumeroProcesso);
		painelDados.add(txtNumeroProcesso);
		painelDados.add(lblNumeroDocumentoModelo);
		painelDados.add(txtNumeroDocumentoModelo);
		painelDados.add(lblUsuario);
		painelDados.add(txtUsuario);
		painelDados.add(lblSenha);
		painelDados.add(txtSenha);
		painelDados.add(btnProcessar); 
		painelDados.add(new JPanel()); 

		SpringUtilities.makeGrid(painelDados,
	            6, 2, //rows, cols
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
								gerarRespostaSEI(txtUsuario.getText(), new String(txtSenha.getPassword()), txtNumeroProcesso.getText(), txtNumeroDocumentoModelo.getText());
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
				String diretorioPadrao = despachoServico.obterConteudoParametro(Parametro.PASTA_PLANILHA_IMPORTACAO);
				if (diretorioPadrao != null && !diretorioPadrao.trim().equals("")) {
					File dirPadrao = new File(diretorioPadrao);
					if (dirPadrao.exists()) {
						filArquivo.setCurrentDirectory(dirPadrao);
					}
				}
				Action detalhes = filArquivo.getActionMap().get("viewTypeDetails");
				detalhes.actionPerformed(null);
				int retorno = filArquivo.showOpenDialog(InclusaoOficioFiscalizacao.this);
				if (retorno == JFileChooser.APPROVE_OPTION) {
					if (filArquivo.getSelectedFile().exists()) {
						lblNomeArquivo.setText(filArquivo.getSelectedFile().getAbsolutePath());
						if (!diretorioPadrao.equals(filArquivo.getSelectedFile().getParent())) {
							despachoServico.salvarConteudoParametro(Parametro.PASTA_PLANILHA_IMPORTACAO, filArquivo.getSelectedFile().getParent());
						}
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

	private void gerarRespostaSEI(String usuario, String senha, String numeroProcesso, String numeroDocumentoModelo) throws Exception {
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

        // obter os dados do superior assinante
		Iterator<Assinante> assinanteIterator = despachoServico.obterAssinante(null, null, true, true).iterator();
		
		if(!assinanteIterator.hasNext()) {
			throw new Exception("Nenhum assinante superior cadastrado.");
		}
		
		superior = assinanteIterator.next();
		
        // And now use this to visit Google
        driver.get(despachoServico.obterConteudoParametro(Parametro.ENDERECO_SEI));

        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(60))
        		.pollingEvery(Duration.ofSeconds(3))
        		.ignoring(NoSuchElementException.class);

        // este wait n�o deve ter seu tempo alterado, pois � usado apenas para buscar a variante <autor> no frame correto; se for aumentada, pode ter impacto negativo em fun��o da quantidade de frames que pode conter um documento
        Wait<WebDriver> wait2 = new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(2))
        		.pollingEvery(Duration.ofSeconds(1))
        		.ignoring(NoSuchElementException.class);

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

		// pesquisa o processo onde dever�o ser inclu�dos os of�cios
		WebElement txtPesquisaRapida = MyUtils.encontrarElemento(wait, By.xpath("//input[@id = 'txtPesquisaRapida']"));
		txtPesquisaRapida.sendKeys(numeroProcesso);
		txtPesquisaRapida.sendKeys(Keys.RETURN);

		Map<String, Oficio> oficiosAGerar = obterDadosOficios(lblNomeArquivo.getText());
		for (Oficio oficio : oficiosAGerar.values()) {
			MyUtils.appendLogArea(logArea, "Gerando of�cio para UG: " + oficio.ugResponsavel);

			// clica em inserir documento
			driver.switchTo().frame(MyUtils.encontrarElemento(wait, By.id("ifrVisualizacao")));
			WebElement btnIncluirDocumento = MyUtils.encontrarElemento(wait, By.xpath("//img[@alt = 'Incluir Documento']"));
			btnIncluirDocumento.click();

			// clica no tipo de documento
			WebElement btnOpcaoTipoDocumento = MyUtils.encontrarElemento(wait, By.xpath("//a[text() = 'Of�cio']"));
			btnOpcaoTipoDocumento.click();

			// clica em documento modelo
			WebElement lblDocumentoModelo = MyUtils.encontrarElemento(wait, By.xpath("//label[contains(text(), 'Documento Modelo')]"));
			lblDocumentoModelo.click();

			// preenche o c�digo do documento modelo
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
			
			// abriu janela para editar o documento, ent�o navega at� a janela
			for (String tituloJanela : driver.getWindowHandles()) {
				driver.switchTo().window(tituloJanela);
			}

			// encontrar o iframe que contem o corpo do documento a ser editado
			driver.switchTo().defaultContent();
			// alterna para o frame de destinat�rio para substituir os dados
			driver.switchTo().frame(3);
			// encontra o primeiro par�grafo e clica nele para mudar o foco para o elemento
			MyUtils.encontrarElemento(wait, By.xpath("(//p)[1]")).click();
			TimeUnit.SECONDS.sleep(1);

			substituirMarcacaoDocumento(driver, wait, oficio.mapaSubstituicoes());
			
				// procura o bot�o salvar, conferindo que ele esteja habilitado
				WebElement btnSalvar = MyUtils.encontrarElemento(wait, By.xpath("//div[contains(@id, 'cke_txaEditor') and contains(@class, 'cke_detached') and not(contains(@style, 'display: none'))]//a[contains(@title, 'Salvar') and not(@aria-disabled)]"));
				btnSalvar.click();
				
				TimeUnit.MILLISECONDS.sleep(500);
				
				// aguarda at� que o bot�o de salvar esteja novamente desabilitado para fechar a janela
				MyUtils.encontrarElemento(wait, By.xpath("//div[contains(@id, 'cke_txaEditor') and contains(@class, 'cke_detached') and not(contains(@style, 'display: none'))]//a[contains(@title, 'Salvar') and @aria-disabled]"));
				
				driver.close();
				driver.switchTo().window(janelaPrincipal);

				// clica no bot�o adicionar ao bloco interno
				respostaAGerar.setBlocoAssinatura(obterBlocoAssinatura(respostaAGerar.getAssinante(), respostaAGerar.getTipoResposta()));
				
				driver.switchTo().frame(MyUtils.encontrarElemento(wait, By.id("ifrVisualizacao")));
				WebElement btnIncluirBlocoAssinatura = MyUtils.encontrarElemento(wait, By.xpath("//img[@alt = 'Incluir em Bloco de Assinatura']"));
				btnIncluirBlocoAssinatura.click();

				// seleciona o bloco interno desejado
				Select cbxBlocoAssinatura = new Select(MyUtils.encontrarElemento(wait, By.id("selBloco")));
				cbxBlocoAssinatura.selectByValue(respostaAGerar.getBlocoAssinatura());

				// aguardar que a linha com o documento gerado seja carregada
				MyUtils.encontrarElemento(wait, By.xpath("//table[@id = 'tblDocumentos']/tbody/tr[./td[2]/a[text() = '" + respostaAGerar.getNumeroDocumentoSEI() + "']]"));

				// clica em incluir
				WebElement btnIncluir = MyUtils.encontrarElemento(wait, By.id("sbmIncluir"));
				btnIncluir.click();

				// aguardar que a linha retorno indicando que o registro est� inserido no bloco
				MyUtils.encontrarElemento(wait, By.xpath("//table[@id = 'tblDocumentos']/tbody/tr[./td[2]/a[text() = '" + respostaAGerar.getNumeroDocumentoSEI() + "'] and ./td[5]/a[text() = '" + respostaAGerar.getBlocoAssinatura() + "']]"));
				
				driver.switchTo().defaultContent();
				
				// atualiza o n�mero do documento gerado no SEI
				atualizarDocumentoGerado(respostaAGerar, superior);
			} // fim do loop de respostas a gerar por unidade
		} // fim do loop de todas as respostas a gerar

		MyUtils.appendLogArea(logArea, "Fim do Processamento...");

        driver.close();
        driver.quit();
	}

	private String obterBlocoAssinatura(Assinante assinante, TipoResposta tipoResposta) throws Exception {
		List<AssinanteTipoResposta> confs = despachoServico.obterAssinanteTipoResposta(null, assinante.getAssinanteId(), tipoResposta.getTipoRespostaId());
		if (confs != null && confs.size() > 0) {
			return confs.iterator().next().getBlocoAssinatura();
		} else {
			return assinante.getBlocoAssinatura();
		}
	}

	private void gerarProcessoIndividual(WebDriver driver, Wait<WebDriver> wait, SolicitacaoResposta resposta, String pastaArquivosProcessosIndividuais) throws Exception {
		String numeroProcesso = null;

		MyUtils.appendLogArea(logArea, "Gerando processo individual...");
		
		// encontrar link de iniciar processo
		WebElement lnkIniciarProcesso = MyUtils.encontrarElemento(wait, By.xpath("//a[text() = 'Iniciar Processo']"));
		lnkIniciarProcesso.click();

		WebElement lnkExibirTodosTipos = null;

		try {
			lnkExibirTodosTipos = MyUtils.encontrarElemento(wait, By.xpath("//img[@title = 'Exibir todos os tipos']"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (lnkExibirTodosTipos != null) {
			lnkExibirTodosTipos.click();
		}

		WebElement lnkTipoProcesso = MyUtils.encontrarElemento(wait, By.xpath("//a[text() = '" + resposta.getTipoResposta().getTipoProcesso() + "']"));
		lnkTipoProcesso.click();

		WebElement txtDescricaoProcesso = MyUtils.encontrarElemento(wait, By.id("txtDescricao"));
		txtDescricaoProcesso.sendKeys(resposta.getSolicitacao().getNumeroProcesso());

		WebElement optNivelAcessoProcesso = MyUtils.encontrarElemento(wait, By.id("optRestrito"));
		optNivelAcessoProcesso.click();
		TimeUnit.MILLISECONDS.sleep(200);
		
		Select cbxHipoteseLegal = new Select(MyUtils.encontrarElemento(wait, By.id("selHipoteseLegal")));
		TimeUnit.MILLISECONDS.sleep(200);
		cbxHipoteseLegal.selectByValue("34");
		TimeUnit.MILLISECONDS.sleep(200);

		WebElement btnSalvarProcesso = MyUtils.encontrarElemento(wait, By.id("btnSalvar"));
		btnSalvarProcesso.click();

		TimeUnit.SECONDS.sleep(2);
		driver.switchTo().defaultContent();

		WebElement ifrArvore = MyUtils.encontrarElemento(wait, By.xpath("//iframe[@id = 'ifrArvore']"));
		driver.switchTo().frame(ifrArvore);
		WebElement txtNumeroProcesso = MyUtils.encontrarElemento(wait, By.xpath("//div[@id = 'topmenu']/a[@target = 'ifrVisualizacao']"));
		numeroProcesso = txtNumeroProcesso.getText();
		MyUtils.appendLogArea(logArea, "Gerado o processo individual n� " + numeroProcesso);

		resposta.getSolicitacao().setNumeroProcessoSEI(numeroProcesso);
		atualizarProcessoGerado(resposta);
		
		driver.switchTo().defaultContent();
	}
	
	public String anexarArquivosProcesso(SolicitacaoResposta resposta, List<File> anexos, WebDriver driver, Wait<WebDriver> wait) throws Exception {
		for (File anexo : anexos) {
			driver.switchTo().defaultContent();

			WebElement txtPesquisaRapida = MyUtils.encontrarElemento(wait, By.id("txtPesquisaRapida"));
			txtPesquisaRapida.sendKeys(resposta.getSolicitacao().getNumeroProcessoSEI());
			txtPesquisaRapida.sendKeys(Keys.RETURN);

			MyUtils.appendLogArea(logArea, "Anexando o arquivo " + anexo.getName());

			// mudar de frame
			WebElement ifrVisualizacao = MyUtils.encontrarElemento(wait, By.xpath("//iframe[@id = 'ifrVisualizacao']"));
			driver.switchTo().frame(ifrVisualizacao);

			// incluir os documentos no processo
			WebElement btnIncluirDocumento = null;
			try {
				btnIncluirDocumento = MyUtils.encontrarElemento(wait, By.xpath("//img[@alt = 'Incluir Documento']"));
			} catch (Exception e) {
				return "N�o foi encontrado o bot�o de incluir documentos no processo " + resposta.getNumeroProcessoSEI() + ". Verifique se este processo est� aberto. Se n�o estiver, reabra-o e processe novamente.";
			}
			btnIncluirDocumento.click();

			WebElement lnkTipoDocumento = MyUtils.encontrarElemento(wait, By.xpath("//a[text() = ' Externo']"));
			lnkTipoDocumento.click();

			Select cbxTipoDocumento = new Select(MyUtils.encontrarElemento(wait, By.id("selSerie")));
			cbxTipoDocumento.selectByVisibleText("Processo");
			TimeUnit.MILLISECONDS.sleep(800);

			WebElement txtDataDocumento = MyUtils.encontrarElemento(wait, By.id("txtDataElaboracao"));
			txtDataDocumento.sendKeys(MyUtils.formatarData(new Date(), "dd/MM/yyyy"));

			WebElement optNatoDigital = MyUtils.encontrarElemento(wait, By.id("optNato"));
			optNatoDigital.click();

			WebElement optNivelAcessoDocumento = MyUtils.encontrarElemento(wait, By.id("optPublico"));
			optNivelAcessoDocumento.click();

			WebElement updArquivo = MyUtils.encontrarElemento(wait, By.id("filArquivo"));
			updArquivo.sendKeys(anexo.getAbsolutePath());

			// loop para esperar que o documento apare�a na lista
			WebElement divDocumentoNaLista = null;
			do {
				try {
					divDocumentoNaLista = MyUtils.encontrarElemento(wait, By.xpath("//table[@id = 'tblAnexos']//tr/td/div[text() = '" + anexo.getName() + "']"));
				} catch (Exception e) {
					divDocumentoNaLista = null;
				}
			} while (divDocumentoNaLista == null);
	
			WebElement btnSalvarDocumento = MyUtils.encontrarElemento(wait, By.id("btnSalvar"));
			btnSalvarDocumento.click();

			TimeUnit.MILLISECONDS.sleep(1500);

			// espera aparecer o bot�o de consultar/alterar documento para ter certeza de que o upload terminou
			MyUtils.encontrarElemento(wait, By.xpath("//img[@title = 'Consultar/Alterar Documento Externo']"));
		} // fim do loop de anexa��o de arquivos
		
		driver.switchTo().defaultContent();
		
		resposta.getSolicitacao().setArquivosAnexados(true);
		atualizarArquivosAnexados(resposta);

		return "";
	}

	private Map<String, String> obterMapaSubstituicoes(SolicitacaoResposta resposta, Assinante superior) {
		Map<String, String> retorno = new LinkedHashMap<String, String>();
		String numeroProcesso = resposta.getSolicitacao().getNumeroProcesso();
		if (resposta.getSolicitacao().getOrigem().getOrigemId().equals(Origem.SPUNET_ID) && resposta.getSolicitacao().getNumeroProcesso().length() == 17) {
			// reformata o n�mero do processo para a m�scara UUUU.NNNNNN/AAAA-DD
			numeroProcesso = numeroProcesso.substring(0, 5) + "." + numeroProcesso.substring(5, 11) + "/" + numeroProcesso.substring(11, 15) + "-" + numeroProcesso.substring(15); 
		}
		retorno.put("<numero_processo>", numeroProcesso);
		retorno.put("<numero_atendimento>", resposta.getSolicitacao().getOrigem().getOrigemId().equals(Origem.SPUNET_ID) ? MyUtils.emptyStringIfNull(resposta.getSolicitacao().getChaveBusca()) : "");
		retorno.put("<autor>", MyUtils.emptyStringIfNull(resposta.getSolicitacao().getAutor()));
		retorno.put("<comarca>", (resposta.getSolicitacao().getMunicipio() == null || resposta.getSolicitacao().getMunicipio().getMunicipioComarca() == null ? "" : resposta.getSolicitacao().getMunicipio().getMunicipioComarca().getNome().toUpperCase()));
		retorno.put("<cartorio>", MyUtils.emptyStringIfNull(resposta.getSolicitacao().getCartorio()));
		String destino = resposta.getSolicitacao().getDestino().getUsarCartorio() ? resposta.getSolicitacao().getCartorio() : resposta.getSolicitacao().getDestino().getDescricao();
		retorno.put("<destino_inicial>", resposta.getSolicitacao().getDestino().getArtigo() + " " + destino);
		if (resposta.getSolicitacao().getTipoImovel().getDescricao().equalsIgnoreCase("rural") && !resposta.getSolicitacao().getEndereco().equalsIgnoreCase("")) {
			retorno.put("<tipo_imovel>", MyUtils.emptyStringIfNull(resposta.getSolicitacao().getEndereco()));
			retorno.put("<endereco>", "");
		} else {
			retorno.put("<tipo_imovel>", (resposta.getSolicitacao().getTipoImovel() == null ? "" : resposta.getSolicitacao().getTipoImovel().getDescricao().toLowerCase()));
			retorno.put("<endereco>", resposta.getSolicitacao().getEndereco() == null || resposta.getSolicitacao().getEndereco().trim().equals("") ? "" : "localizado na " + resposta.getSolicitacao().getEndereco() + ", ");
		}
		retorno.put("<municipio>", resposta.getSolicitacao().getMunicipio() == null ? "" : resposta.getSolicitacao().getMunicipio().getNome());
		retorno.put("<area>", MyUtils.emptyStringIfNull(resposta.getSolicitacao().getArea()).trim().equals("") ? "" : "com �rea de " + resposta.getSolicitacao().getArea() + ", ");
		retorno.put("<coordenada>", MyUtils.emptyStringIfNull(resposta.getSolicitacao().getCoordenada()).trim().equals("") ? "" : "cuja poligonal possui um dos v�rtices com coordenada " + resposta.getSolicitacao().getCoordenada() + ", ");
		retorno.put("<destino_final>", resposta.getSolicitacao().getDestino().getArtigo().toLowerCase() + " " + (destino.startsWith("Procuradoria") ? "Procuradoria" : destino));
		retorno.put("<assinante>", resposta.getAssinante().getNome());
		retorno.put("<assinante_cargo>", resposta.getAssinante().getCargo());
		retorno.put("<assinante_setor>", resposta.getAssinante().getSetor());
		retorno.put("<assinante_superior>", superior.getNome());
		retorno.put("<assinante_superior_cargo>", superior.getCargo());
		retorno.put("<assinante_superior_setor>", superior.getSetor());
		retorno.put("<observacao>", MyUtils.emptyStringIfNull(resposta.getObservacao().trim()));
		retorno.put("<data_hoje>", MyUtils.formatarData(new Date(), "dd 'de' MMMM 'de' yyyy").toLowerCase());
		return retorno;
	}

	private void atualizarProcessoGerado(SolicitacaoResposta resposta) throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("update solicitacao "
				 + "   set numeroprocessosei = '" + resposta.getSolicitacao().getNumeroProcessoSEI() + "' "
				 + "	 , arquivosanexados = false "
				 + " where solicitacaoid = " + resposta.getSolicitacao().getSolicitacaoId());

		JPAUtils.executeUpdate(conexao, sql.toString());
	}

	private void atualizarArquivosAnexados(SolicitacaoResposta resposta) throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("update solicitacao "
				 + "   set arquivosanexados = true "
				 + " where solicitacaoid = " + resposta.getSolicitacao().getSolicitacaoId());

		JPAUtils.executeUpdate(conexao, sql.toString());
	}

	private void atualizarDocumentoGerado(SolicitacaoResposta resposta, Assinante superior) throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("update solicitacaoresposta "
				 + "   set numerodocumentosei = '" + resposta.getNumeroDocumentoSEI() + "'"
				 + (MyUtils.isPostgreSQL(conexao) 
					? "	 , datahoraresposta = now() "
					: "	 , datahoraresposta = datetime('now', 'localtime') "
				)
				 + "	 , numeroprocessosei = '" + resposta.getNumeroProcessoSEI() + "' "
				 + "	 , respostaimpressa = " + (resposta.getTipoResposta().getImprimirResposta() ? "false" : "true")
				 + "	 , blocoassinatura = '" + resposta.getBlocoAssinatura() + "' "
				 + "	 , respostanoblocoassinatura = true "
				 + "     , assinanteidsuperior = " + superior.getAssinanteId()
				 + " where solicitacaorespostaid = " + resposta.getSolicitacaoRespostaId());

		JPAUtils.executeUpdate(conexao, sql.toString());
	}

	private Map<String, List<SolicitacaoResposta>> obterRespostasACadastrar(Integer assinanteId) throws Exception {
		Map<String, List<SolicitacaoResposta>> retorno = new TreeMap<String, List<SolicitacaoResposta>>();
		Assinante assinante = null;
		if (assinanteId != null && assinanteId.intValue() > 0) {
			assinante = new Assinante(assinanteId, null);
		}

		List<SolicitacaoResposta> respostas = despachoServico.obterRespostasAGerar(assinante);
		
		for (SolicitacaoResposta resposta : respostas) {
			String unidadeAberturaProcesso = resposta.getTipoResposta().getUnidadeAberturaProcesso();
			if (unidadeAberturaProcesso == null) unidadeAberturaProcesso = "";
			if (retorno.get(unidadeAberturaProcesso) == null) retorno.put(unidadeAberturaProcesso, new ArrayList<SolicitacaoResposta>());
			retorno.get(unidadeAberturaProcesso).add(resposta);
		}
		
		return retorno;
	}

	private List<File> obterArquivos(String pasta, String filtroNomeArquivo, String filtroExtensao) {
		FilenameFilter filtro = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				boolean atendeNome = true;
				boolean atendeExtensao = true;
				if (filtroNomeArquivo != null && !filtroNomeArquivo.equalsIgnoreCase("")) {
					atendeNome = name.toLowerCase().contains(filtroNomeArquivo.toLowerCase());
				}
				if (filtroExtensao != null && !filtroExtensao.equalsIgnoreCase("")) {
					atendeExtensao = name.toLowerCase().endsWith(filtroExtensao.toLowerCase());
				}
				return atendeNome && atendeExtensao;
			}
		};
		File diretorio = new File(pasta);
		return Arrays.asList(diretorio.listFiles(filtro));
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
	}
	
	private class Oficio {
		String ugResponsavel;
		String tratamentoDestinatario;
		String nomeDestinatario;
		String cargoDestinatario;
		String pessoaJuridica;
		String enderecoDestinatario;
		String complementoEnderecoDestinatario;
		String emailDestinatario;
		List<Imovel> listaImoveis = new ArrayList<Imovel>();

		public Oficio(String ugResponsavel, String tratamentoDestinatario, String nomeDestinatario, String cargoDestinatario, String pessoaJuridica, String enderecoDestinatario, String complementoEnderecoDestinatario, String emailDestinatario) {
			this.ugResponsavel = ugResponsavel;
			this.tratamentoDestinatario = tratamentoDestinatario;
			this.nomeDestinatario = nomeDestinatario;
			this.cargoDestinatario = cargoDestinatario;
			this.pessoaJuridica = pessoaJuridica;
			this.enderecoDestinatario = enderecoDestinatario;
			this.complementoEnderecoDestinatario = complementoEnderecoDestinatario;
			this.emailDestinatario = emailDestinatario;
		}
		
		public Map<String, String> mapaSubstituicoes() {
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
			// caso a chave lida seja em branco, considera que � igual � ultima chave lida (conceito mestre-detalhe para UG Respons�vel-Im�vel)
			if (chaveAtual.equals("")) chaveAtual = chaveAnterior;
			
			// obtem o of�cio do mapa
			Oficio oficio = retorno.get(chaveAtual);
			
			// se o of�cio lido n�o foi encontrado, gera um novo of�cio, preenchendo seus dados
			if (!chaveAnterior.equals(chaveAtual)) {
				oficio = new Oficio(
						ugResponsavel, 
						MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(1))).trim(),
						MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(2))).trim(),
						MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(3))).trim(),
						MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(4))).trim(),
						MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(5))).trim(),
						MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(6))).trim(),
						MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(7))).trim()
						);
				
				retorno.put(chaveAtual, oficio);
			}
			
			oficio.adicionaImovel(
					MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(8))).trim(), 
					MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(9))).trim(),
					MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(10))).trim(),
					MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(11))).trim(),
					MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(12))).trim(),
					MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(13))).trim()
					);
			MyUtils.appendLogArea(logArea, "Lendo a linha " + (l+1) + "/" + (planilha.getLastRowNum()+1) + "...");
			chaveAnterior = chaveAtual;
		}
		MyUtils.appendLogArea(logArea, "Fim de leitura da planilha!");
		wb.close();

		return retorno;
	}

	private void substituirMarcacaoDocumento(WebDriver driver, Wait<WebDriver> wait, Map<String, String> mapaSubstituicoes) {
		// volta ao conte�do default
		driver.switchTo().defaultContent();

		// clica no bot�o localizar
		WebElement btnSubstituir = MyUtils.encontrarElemento(wait, By.xpath("//div[contains(@id, 'cke_txaEditor') and contains(@class, 'cke_detached') and not(contains(@style, 'display: none'))]//a[@title = 'Substituir']"));
		btnSubstituir.click();

		// repetir este peda�o para todos os textos a serem substitu�dos no documento
		for (String chave : mapaSubstituicoes.keySet()) {
			String textoSubstituto = mapaSubstituicoes.get(chave);

			// preenche o texto a ser encontrado
			WebElement txtPesquisar = MyUtils.encontrarElemento(wait, By.xpath("(//div[@role = 'tabpanel' and not(contains(@style, 'display: none'))]//input[@type = 'text'])[1]"));
			txtPesquisar.clear();
			txtPesquisar.sendKeys(chave);
			
			// preenche o texto para substitui��o
			WebElement txtSubstituir = MyUtils.encontrarElemento(wait, By.xpath("(//div[@role = 'tabpanel' and not(contains(@style, 'display: none'))]//input[@type = 'text'])[2]"));
			txtSubstituir.clear();
			txtSubstituir.sendKeys(textoSubstituto);
			
			// clica em substituir tudo
			WebElement btnSubstituirTudo = MyUtils.encontrarElemento(wait, By.xpath("//a[@title = 'Substituir Tudo']"));
			btnSubstituirTudo.click();
			
			// clica em ok na mensagem apresentada
			driver.switchTo().alert().accept();
		}
		
		// clica em fechar
		WebElement btnFechar = MyUtils.encontrarElemento(wait, By.xpath("//span[text() = 'Fechar']"));
		btnFechar.click();
	}
}
