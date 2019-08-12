import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;

import framework.MyUtils;
import framework.SpringUtilities;

@SuppressWarnings("serial")
public class InclusaoDespachoSEI extends JInternalFrame {

	private Connection conexao;
	private JFileChooser filSelecionarPasta = new JFileChooser();
	private JButton btnAbrirJanelaSelecaoPasta = new JButton("Selecionar pasta");
	private JLabel lblPastaProcessosIndividuais = new JLabel("") {{ setVerticalTextPosition(SwingConstants.TOP); setSize(600, 20); }};
	private JLabel lblSelecionarPasta = new JLabel("Pasta:", JLabel.TRAILING) {{ setLabelFor(filSelecionarPasta); }};
	private JTextField txtUsuario = new JTextField(15);
	private JLabel lblUsuario = new JLabel("Usuário:") {{ setLabelFor(txtUsuario); }};
	private JPasswordField txtSenha = new JPasswordField(15);
	private JLabel lblSenha = new JLabel("Senha:") {{ setLabelFor(txtSenha); }};
	private JPanel painelDados = new JPanel() {{ setLayout(new SpringLayout()); }};
	private JButton btnProcessar = new JButton("Processar"); 
	private JTextArea logArea = new JTextArea(30, 100);
	private JScrollPane areaDeRolagem = new JScrollPane(logArea);
	private DespachoServico despachoServico;
	private Assinante superior;

	public InclusaoDespachoSEI(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		this.conexao = conexao;
		despachoServico = new DespachoServico(conexao);

		lblPastaProcessosIndividuais.setText(despachoServico.obterConteudoParametro(Parametro.PASTA_ARQUIVOS_PROCESSOS_INDIVIDUAIS));
		JPanel painelArquivo = new JPanel() {{ add(lblSelecionarPasta); add(btnAbrirJanelaSelecaoPasta); }};

		painelDados.add(painelArquivo);
		painelDados.add(lblPastaProcessosIndividuais);
		painelDados.add(lblUsuario);
		painelDados.add(txtUsuario);
		painelDados.add(lblSenha);
		painelDados.add(txtSenha);
		painelDados.add(btnProcessar); 
		painelDados.add(new JPanel()); 
		
		SpringUtilities.makeGrid(painelDados,
	            4, 2, //rows, cols
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
								gerarDespachoSEI(txtUsuario.getText(), new String(txtSenha.getPassword()), lblPastaProcessosIndividuais.getText());
							} catch (Exception e) {
								MyUtils.appendLogArea(logArea, "Erro ao gerar os despachos no SEI: \n \n" + e.getMessage() + "\n" + stackTraceToString(e));
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

		btnAbrirJanelaSelecaoPasta.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String diretorioPadrao = despachoServico.obterConteudoParametro(Parametro.PASTA_ARQUIVOS_PROCESSOS_INDIVIDUAIS);
				if (diretorioPadrao != null && !diretorioPadrao.trim().equals("")) {
					File dirPadrao = new File(diretorioPadrao);
					if (dirPadrao.exists()) {
						filSelecionarPasta.setCurrentDirectory(dirPadrao);
					}
				}
				filSelecionarPasta.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				filSelecionarPasta.setAcceptAllFileFilterUsed(false);
				int retorno = filSelecionarPasta.showOpenDialog(InclusaoDespachoSEI.this);
				if (retorno == JFileChooser.APPROVE_OPTION) {
					if (filSelecionarPasta.getSelectedFile().exists()) {
						lblPastaProcessosIndividuais.setText(filSelecionarPasta.getSelectedFile().getAbsolutePath());
						if (diretorioPadrao == null || !diretorioPadrao.equals(filSelecionarPasta.getSelectedFile().getAbsolutePath())) {
							despachoServico.salvarConteudoParametro(Parametro.PASTA_ARQUIVOS_PROCESSOS_INDIVIDUAIS, filSelecionarPasta.getSelectedFile().getAbsolutePath());
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

	private void gerarDespachoSEI(String usuario, String senha, String pastaProcessosIndividuais) throws Exception {
		if (pastaProcessosIndividuais == null || pastaProcessosIndividuais.equalsIgnoreCase("")) {
			throw new Exception("É necessário informar a pasta onde estão os arquivos a serem anexados nos processos que forem abertos individualmente.");
		}
		
		MyUtils.appendLogArea(logArea, "Iniciando o navegador web...");
		System.setProperty("webdriver.chrome.driver", MyUtils.chromeWebDriverPath());
        WebDriver driver = new ChromeDriver();

        // obter os dados do superior assinante
		superior = despachoServico.obterAssinante(null, null, Boolean.TRUE).iterator().next();
		
        // And now use this to visit Google
        driver.get(despachoServico.obterConteudoParametro(Parametro.ENDERECO_SEI));

        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(30))
        		.pollingEvery(Duration.ofSeconds(3))
        		.ignoring(NoSuchElementException.class);

        Wait<WebDriver> wait3 = new FluentWait<WebDriver>(driver)
        		.withTimeout(Duration.ofSeconds(3))
        		.pollingEvery(Duration.ofSeconds(1))
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
        MyUtils.fecharPopup(driver);
        String janelaPrincipal = driver.getWindowHandle();

        // selecionar a unidade default
        MyUtils.selecionarUnidade(driver, wait, despachoServico.obterConteudoParametro(Parametro.UNIDADE_PADRAO_SEI));
        
		Map<String, List<Despacho>> despachosAGerar = obterDespachosACadastrar();
		for (String unidadeAberturaProcesso : despachosAGerar.keySet()) {
			List<Despacho> despachosDaUnidade = despachosAGerar.get(unidadeAberturaProcesso);

			if (!unidadeAberturaProcesso.trim().equals("")) {
				MyUtils.selecionarUnidade(driver, wait, unidadeAberturaProcesso);
			}

			for (Despacho despachoAGerar : despachosDaUnidade) {
				// processamento....
				MyUtils.appendLogArea(logArea, "Processo: " + despachoAGerar.getNumeroProcesso());
	
				if (despachoAGerar.getTipoDespacho().getGerarProcessoIndividual()) {
					List<File> anexos = obterArquivos(pastaProcessosIndividuais, despachoAGerar.getNumeroProcesso(), null);
					if (despachoAGerar.getNumeroProcessoSEI() == null || despachoAGerar.getNumeroProcessoSEI().trim().equalsIgnoreCase("")) {
						if (anexos == null || anexos.size() == 0) {
							MyUtils.appendLogArea(logArea, "Não foi possível gerar o processo individual, pois não foi encontrado nenhum arquivo referente ao processo.");
							continue;
						}
	
						gerarProcessoIndividual(driver, wait, despachoAGerar, pastaProcessosIndividuais);
					}
	
					if (!despachoAGerar.getArquivosAnexados()) {
						anexarArquivosProcesso(despachoAGerar, anexos, driver, wait);
					}

					despachoAGerar.setBlocoAssinatura(obterBlocoAssinatura(despachoAGerar.getAssinante(), despachoAGerar.getTipoDespacho()));
				} else {
					despachoAGerar.setNumeroProcessoSEI(despachoAGerar.getAssinante().getNumeroProcesso());
					despachoAGerar.setBlocoAssinatura(despachoAGerar.getAssinante().getBlocoAssinatura());
				}
	
				// pesquisa o processo onde deverá ser incluído o despacho
				WebElement txtPesquisaRapida = MyUtils.encontrarElemento(wait, By.xpath("//input[@id = 'txtPesquisaRapida']"));
				txtPesquisaRapida.sendKeys(despachoAGerar.getNumeroProcessoSEI());
				txtPesquisaRapida.sendKeys(Keys.RETURN);
				
				// clica em inserir documento
				driver.switchTo().frame(MyUtils.encontrarElemento(wait, By.id("ifrVisualizacao")));
				WebElement btnIncluirDocumento = MyUtils.encontrarElemento(wait, By.xpath("//img[@alt = 'Incluir Documento']"));
				btnIncluirDocumento.click();
				
				// clica em despacho
				WebElement btnOpcaoDespacho = MyUtils.encontrarElemento(wait, By.xpath("//a[text() = 'Despacho']"));
				btnOpcaoDespacho.click();
	
				// clica em documento modelo
				WebElement lblDocumentoModelo = MyUtils.encontrarElemento(wait, By.xpath("//label[contains(text(), 'Documento Modelo')]"));
				lblDocumentoModelo.click();
	
				// preenche o código do documento modelo
				WebElement txtCodigoDocumentoModelo = MyUtils.encontrarElemento(wait, By.xpath("//input[@id = 'txtProtocoloDocumentoTextoBase']"));
				txtCodigoDocumentoModelo.sendKeys(despachoAGerar.getTipoDespacho().getNumeroDocumentoSEI());
	
				// seleciona nivel de acesso do despacho
				WebElement lblNivelAcessoPublico = MyUtils.encontrarElemento(wait, By.xpath("//label[@id = 'lblPublico']"));
				lblNivelAcessoPublico.click();
				
				// clica em confirmar dados
				WebElement btnConfirmarDados = MyUtils.encontrarElemento(wait, By.xpath("//button[@id = 'btnSalvar']"));
				btnConfirmarDados.click();
				
				// abriu janela para editar o documento, então navega até a janela
				for (String tituloJanela : driver.getWindowHandles()) {
					driver.switchTo().window(tituloJanela);
				}
	
				despachoAGerar.setNumeroDocumentoSEI(driver.getTitle().split(" - ")[1]);
				MyUtils.appendLogArea(logArea, "Nº Documento Gerado: " + despachoAGerar.getNumeroDocumentoSEI());
	
				// encontrar o iframe que contem o corpo do despacho a ser editado
				driver.switchTo().defaultContent();
				List<WebElement> frmIFrames = null;
				int espera = 15;
				do {
					TimeUnit.SECONDS.sleep(2);
					frmIFrames = MyUtils.encontrarElementos(wait, By.tagName("iframe"));
				} while (--espera >= 0 && (frmIFrames == null || frmIFrames.size() <= 1));
	
				WebElement welNumeroProcesso = null;
				
				for (WebElement frmIFrame : frmIFrames) {
					driver.switchTo().frame(frmIFrame);
					
					try {
						welNumeroProcesso = MyUtils.encontrarElemento(wait3, By.xpath("//*[contains(text(), '<autor>')]"));
					} catch (Exception e) {
						welNumeroProcesso = null;
					}
	
					if (welNumeroProcesso != null) {
						break;
					} else {
						driver.switchTo().defaultContent();
					}
				}
				
				// clica no primeiro paragrafo encontrado no iframe
				welNumeroProcesso.click();
				
				// volta ao conteúdo default
				driver.switchTo().defaultContent();
				
				// clica no botão localizar
				WebElement btnLocalizar = MyUtils.encontrarElemento(wait, By.xpath("//div[contains(@id, 'cke_txaEditor') and contains(@class, 'cke_detached') and not(contains(@style, 'display: none'))]//a[@title = 'Localizar']"));
				btnLocalizar.click();
	
				// clica na aba substituir
				WebElement tabSubstituir = MyUtils.encontrarElemento(wait, By.xpath("//a[contains(text(), 'Substituir')]"));
				TimeUnit.SECONDS.sleep(1);
				tabSubstituir.click();
	
				Map<String, String> mapaSubstituicoes = obterMapaSubstituicoes(despachoAGerar, superior);
				
				// repetir este pedaço para todos os textos a serem substituídos no documento
				for (String chave : mapaSubstituicoes.keySet()) {
					String textoSubstituto = mapaSubstituicoes.get(chave);
	
					// appendLogArea(logArea, "Substituindo '" + chave + "' por '" + textoSubstituto + "'");
					
					// preenche o texto a ser encontrado
					WebElement txtPesquisar = MyUtils.encontrarElemento(wait, By.xpath("(//div[@role = 'tabpanel' and not(contains(@style, 'display: none'))]//input[@type = 'text'])[1]"));
					txtPesquisar.clear();
					txtPesquisar.sendKeys(chave);
					
					// preenche o texto para substituição
					WebElement txtSubstituir = MyUtils.encontrarElemento(wait, By.xpath("(//div[@role = 'tabpanel' and not(contains(@style, 'display: none'))]//input[@type = 'text'])[2]"));
					txtSubstituir.clear();
					txtSubstituir.sendKeys(textoSubstituto);
					
					// clica em substituir tudo
					WebElement btnSubstituirTudo = MyUtils.encontrarElemento(wait, By.xpath("//a[@title = 'Substituir Tudo']"));
					btnSubstituirTudo.click();
					
					// clica em ok na mensagem apresentada
					// appendLogArea(logArea, "Resultado da substituição: " + driver.switchTo().alert().getText());
					driver.switchTo().alert().accept();
				}
				
				// clica em fechar
				WebElement btnFechar = MyUtils.encontrarElemento(wait, By.xpath("//span[text() = 'Fechar']"));
				btnFechar.click();
	
				// clica no botão salvar
				WebElement btnSalvar = MyUtils.encontrarElemento(wait, By.xpath("//div[contains(@id, 'cke_txaEditor') and contains(@class, 'cke_detached') and not(contains(@style, 'display: none'))]//a[contains(@title, 'Salvar')]"));
				btnSalvar.click();
				
				driver.close();
				driver.switchTo().window(janelaPrincipal);
				
				// clica no botão adicionar ao bloco interno
				driver.switchTo().frame(MyUtils.encontrarElemento(wait, By.id("ifrVisualizacao")));
				WebElement btnIncluirBlocoAssinatura = MyUtils.encontrarElemento(wait, By.xpath("//img[@alt = 'Incluir em Bloco de Assinatura']"));
				btnIncluirBlocoAssinatura.click();
				
				// seleciona o bloco interno desejado
				Select cbxBlocoAssinatura = new Select(MyUtils.encontrarElemento(wait, By.id("selBloco")));
				cbxBlocoAssinatura.selectByValue(despachoAGerar.getBlocoAssinatura());
				
				// clica em incluir
				WebElement btnIncluir = MyUtils.encontrarElemento(wait, By.id("sbmIncluir"));
				btnIncluir.click();
				
				driver.switchTo().defaultContent();
				
				// atualiza o número do despacho gerado no SEI
				atualizarDespachoGerado(despachoAGerar);
			} // fim do loop de despachos a gerar por unidade
		} // fim do loop de todos os despachos a gerar

		MyUtils.appendLogArea(logArea, "Fim do Processamento...");

        driver.close();
        driver.quit();
	}

	private String obterBlocoAssinatura(Assinante assinante, TipoDespacho tipoDespacho) throws Exception {
		List<AssinanteTipoDespacho> confs = despachoServico.obterAssinanteTipoDespacho(null, assinante.getAssinanteId(), tipoDespacho.getTipoDespachoId());
		if (confs != null && confs.size() > 0) {
			return confs.iterator().next().getBlocoAssinatura();
		} else {
			return assinante.getBlocoAssinatura();
		}
	}

	private void gerarProcessoIndividual(WebDriver driver, Wait<WebDriver> wait, Despacho despacho, String pastaArquivosProcessosIndividuais) throws Exception {
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

		WebElement lnkTipoProcesso = MyUtils.encontrarElemento(wait, By.xpath("//a[text() = '" + despacho.getTipoDespacho().getTipoProcesso() + "']"));
		lnkTipoProcesso.click();

		WebElement txtDescricaoProcesso = MyUtils.encontrarElemento(wait, By.id("txtDescricao"));
		txtDescricaoProcesso.sendKeys(despacho.getNumeroProcesso());

//		WebElement txtInteressado = MyUtils.encontrarElemento(wait, By.id("txtInteressadoProcedimento"));
//		txtInteressado.sendKeys(despacho.getAutor());

		WebElement optNivelAcessoProcesso = MyUtils.encontrarElemento(wait, By.id("optPublico"));
		optNivelAcessoProcesso.click();
		
		WebElement btnSalvarProcesso = MyUtils.encontrarElemento(wait, By.id("btnSalvar"));
		btnSalvarProcesso.click();

		TimeUnit.SECONDS.sleep(2);
		driver.switchTo().defaultContent();

		WebElement ifrArvore = MyUtils.encontrarElemento(wait, By.xpath("//iframe[@id = 'ifrArvore']"));
		driver.switchTo().frame(ifrArvore);
		WebElement txtNumeroProcesso = MyUtils.encontrarElemento(wait, By.xpath("//div[@id = 'topmenu']/a[@target = 'ifrVisualizacao']"));
		numeroProcesso = txtNumeroProcesso.getText();
		MyUtils.appendLogArea(logArea, "Gerado o processo individual nº " + numeroProcesso);

		despacho.setNumeroProcessoSEI(numeroProcesso);
		atualizarProcessoGerado(despacho);
		
		driver.switchTo().defaultContent();
	}
	
	public void anexarArquivosProcesso(Despacho despacho, List<File> anexos, WebDriver driver, Wait<WebDriver> wait) throws Exception {
		for (File anexo : anexos) {
			driver.switchTo().defaultContent();

			WebElement txtPesquisaRapida = MyUtils.encontrarElemento(wait, By.id("txtPesquisaRapida"));
			txtPesquisaRapida.sendKeys(despacho.getNumeroProcessoSEI());
			txtPesquisaRapida.sendKeys(Keys.RETURN);

			MyUtils.appendLogArea(logArea, "Anexando o arquivo " + anexo.getName());

			// mudar de frame
			WebElement ifrVisualizacao = MyUtils.encontrarElemento(wait, By.xpath("//iframe[@id = 'ifrVisualizacao']"));
			driver.switchTo().frame(ifrVisualizacao);

			// incluir os documentos no processo
			WebElement btnIncluirDocumento = MyUtils.encontrarElemento(wait, By.xpath("//img[@title = 'Incluir Documento']"));
			btnIncluirDocumento.click();

			WebElement lnkTipoDocumento = MyUtils.encontrarElemento(wait, By.xpath("//a[text() = ' Externo']"));
			lnkTipoDocumento.click();

			Select cbxTipoDocumento = new Select(MyUtils.encontrarElemento(wait, By.id("selSerie")));
			cbxTipoDocumento.selectByVisibleText("Processo");

			WebElement txtDataDocumento = MyUtils.encontrarElemento(wait, By.id("txtDataElaboracao"));
			txtDataDocumento.sendKeys(MyUtils.formatarData(new Date(), "dd/MM/yyyy"));

			WebElement optNatoDigital = MyUtils.encontrarElemento(wait, By.id("optNato"));
			optNatoDigital.click();

			WebElement optNivelAcessoDocumento = MyUtils.encontrarElemento(wait, By.id("optPublico"));
			optNivelAcessoDocumento.click();

			WebElement updArquivo = MyUtils.encontrarElemento(wait, By.id("filArquivo"));
			updArquivo.sendKeys(anexo.getAbsolutePath());

			// loop para esperar que o documento apareça na lista
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

			// espera aparecer o botão de cancelar documento para ter certeza de que o upload terminou
			MyUtils.encontrarElemento(wait, By.xpath("//img[@title = 'Cancelar Documento']"));
		} // fim do loop de anexação de arquivos
		
		driver.switchTo().defaultContent();
		
		despacho.setArquivosAnexados(true);
		atualizarArquivosAnexados(despacho);
	}

	private Map<String, String> obterMapaSubstituicoes(Despacho despacho, Assinante superior) {
		Map<String, String> retorno = new LinkedHashMap<String, String>();
		retorno.put("<numero_processo>", despacho.getNumeroProcesso());
		retorno.put("<autor>", despacho.getAutor());
		retorno.put("<comarca>", despacho.getComarca().toUpperCase());
		String destino = despacho.getDestino().getUsarComarca() ? despacho.getComarca() : despacho.getDestino().getDescricao();
		retorno.put("<destino_inicial>", despacho.getDestino().getArtigo() + " " + destino);
		if (despacho.getTipoImovel().getDescricao().equalsIgnoreCase("rural") && !despacho.getEndereco().equalsIgnoreCase("")) {
			retorno.put("<tipo_imovel>", despacho.getEndereco());
			retorno.put("<endereco>", "");
		} else {
			retorno.put("<tipo_imovel>", despacho.getTipoImovel().getDescricao().toLowerCase());
			retorno.put("<endereco>", despacho.getEndereco() == null || despacho.getEndereco().trim().equals("") ? "" : "localizado na " + despacho.getEndereco() + ", ");
		}
		retorno.put("<municipio>", despacho.getMunicipio());
		retorno.put("<area>", despacho.getArea() == null || despacho.getArea().trim().equals("") ? "" : "com área de " + despacho.getArea() + ", ");
		retorno.put("<coordenada>", despacho.getCoordenada() == null || despacho.getCoordenada().trim().equals("") ? "" : "cuja poligonal possui um dos vértices com coordenada " + despacho.getCoordenada() + ", ");
		retorno.put("<destino_final>", despacho.getDestino().getArtigo().toLowerCase() + " " + (destino.startsWith("Procuradoria") ? "Procuradoria" : destino));
		retorno.put("<assinante>", despacho.getAssinante().getNome());
		retorno.put("<assinante_cargo>", despacho.getAssinante().getCargo());
		retorno.put("<assinante_setor>", despacho.getAssinante().getSetor());
		retorno.put("<assinante_superior>", superior.getNome());
		retorno.put("<assinante_superior_cargo>", superior.getCargo());
		retorno.put("<assinante_superior_setor>", superior.getSetor());
		retorno.put("<observacao>", despacho.getObservacao() == null || despacho.getObservacao().trim().equals("") ? "" : despacho.getObservacao());
		return retorno;
	}

	private void atualizarProcessoGerado(Despacho despacho) throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("update despacho "
				 + "   set numeroprocessosei = '" + despacho.getNumeroProcessoSEI() + "' "
				 + "	 , arquivosanexados = false "
				 + " where despachoid = " + despacho.getDespachoId());

		MyUtils.execute(conexao, sql.toString());
	}

	private void atualizarArquivosAnexados(Despacho despacho) throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("update despacho "
				 + "   set arquivosanexados = true "
				 + " where despachoid = " + despacho.getDespachoId());

		MyUtils.execute(conexao, sql.toString());
	}

	private void atualizarDespachoGerado(Despacho despacho) throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("update despacho"
				 + "   set numerodocumentosei = '" + despacho.getNumeroDocumentoSEI() + "'"
				 + "	 , datahoradespacho = datetime('now', 'localtime') "
				 + "	 , numeroprocessosei = '" + despacho.getNumeroProcessoSEI() + "' "
				 + "	 , despachoimpresso = false "
				 + "	 , blocoassinatura = '" + despacho.getBlocoAssinatura() + "' "
				 + "	 , despachonoblocoassinatura = true "
				 + " where despachoid = " + despacho.getDespachoId());

		MyUtils.execute(conexao, sql.toString());
	}

	private Map<String, List<Despacho>> obterDespachosACadastrar() throws Exception {
		Map<String, List<Despacho>> retorno = new TreeMap<String, List<Despacho>>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select d.*, coalesce(td.unidadeaberturaprocesso, '') as unidadeaberturaprocesso "
				 + "  from despacho d "
				 + " inner join tipodespacho td using (tipodespachoid) "
				 + " where d.tipodespachoid is not null "
				 + "   and coalesce(d.numerodocumentosei, '') = '' ");

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			String unidadeAberturaProcesso = rs.getString("unidadeaberturaprocesso");
			if (retorno.get(unidadeAberturaProcesso) == null) retorno.put(unidadeAberturaProcesso, new ArrayList<Despacho>());
			retorno.get(unidadeAberturaProcesso).add(new Despacho(rs.getInt("despachoid"),
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
}
