import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;

import framework.JPAUtils;
import framework.MyUtils;
import framework.SpringUtilities;

@SuppressWarnings("serial")
public class InclusaoDespachoSEI extends JInternalFrame {

	private EntityManager conexao;
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

	public InclusaoDespachoSEI(String tituloJanela, EntityManager conexao) {
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
								gerarRespostaSEI(txtUsuario.getText(), new String(txtSenha.getPassword()));
							} catch (Exception e) {
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
	}

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void gerarRespostaSEI(String usuario, String senha) throws Exception {
		String msgVldPastaAssinante = validarPastaProcessoIndividual();
        if (!msgVldPastaAssinante.equals("")) {
        	JOptionPane.showMessageDialog(null, msgVldPastaAssinante);
        	return;
        }

		MyUtils.appendLogArea(logArea, "Iniciando o navegador web...");
		System.setProperty("webdriver.chrome.driver", MyUtils.chromeWebDriverPath());
        WebDriver driver = new ChromeDriver();

        // obter os dados do superior assinante
		superior = despachoServico.obterAssinante(null, null, true, true).iterator().next();
		
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
        
		Map<String, List<SolicitacaoResposta>> respostasAGerar = obterRespostasACadastrar();
		for (String unidadeAberturaProcesso : respostasAGerar.keySet()) {
			List<SolicitacaoResposta> respostasDaUnidade = respostasAGerar.get(unidadeAberturaProcesso);

			if (!unidadeAberturaProcesso.trim().equals("")) {
				MyUtils.selecionarUnidade(driver, wait, unidadeAberturaProcesso);
			}

			for (SolicitacaoResposta respostaAGerar : respostasDaUnidade) {
				// processamento....
				MyUtils.appendLogArea(logArea, "Processo: " + respostaAGerar.getSolicitacao().getNumeroProcesso());

				// verifica se há pendências
				if (!respostaAGerar.getPendenciasParaGeracao().trim().equals("")) {
					MyUtils.appendLogArea(logArea, "A resposta possui pendências de informação e não pode ser gerada automaticamente até que sejam resolvidas: \n" + respostaAGerar.getPendenciasParaGeracao());
					continue;
				}

				if (respostaAGerar.getTipoResposta().getGerarProcessoIndividual()) {
					List<File> anexos = obterArquivos(respostaAGerar.getAssinante().getPastaArquivoProcesso(), respostaAGerar.getSolicitacao().getNumeroProcesso(), null);
					if (MyUtils.emptyStringIfNull(respostaAGerar.getSolicitacao().getNumeroProcessoSEI()).trim().equalsIgnoreCase("")) {
						if (anexos == null || anexos.size() == 0) {
							MyUtils.appendLogArea(logArea, "Não foi possível gerar o processo individual, pois não foi encontrado nenhum arquivo referente ao processo.");
							continue;
						}
	
						gerarProcessoIndividual(driver, wait, respostaAGerar, respostaAGerar.getAssinante().getPastaArquivoProcesso());
					}
	
					if (!respostaAGerar.getSolicitacao().getArquivosAnexados()) {
						String msgAnexarArquivos = anexarArquivosProcesso(respostaAGerar, anexos, driver, wait);
						if (!msgAnexarArquivos.equals("")) {
							MyUtils.appendLogArea(logArea, msgAnexarArquivos);
							continue;
						}
					}

					respostaAGerar.setNumeroProcessoSEI(respostaAGerar.getSolicitacao().getNumeroProcessoSEI());
				} else {
					if (MyUtils.emptyStringIfNull(respostaAGerar.getSolicitacao().getNumeroProcessoSEI()).equals("")) {
						respostaAGerar.setNumeroProcessoSEI(respostaAGerar.getAssinante().getNumeroProcessoSEI());
					} else {
						respostaAGerar.setNumeroProcessoSEI(respostaAGerar.getSolicitacao().getNumeroProcessoSEI());
					}
				}

				// pesquisa o processo onde deverá ser incluído a resposta
				WebElement txtPesquisaRapida = MyUtils.encontrarElemento(wait, By.xpath("//input[@id = 'txtPesquisaRapida']"));
				txtPesquisaRapida.sendKeys(respostaAGerar.getNumeroProcessoSEI());
				txtPesquisaRapida.sendKeys(Keys.RETURN);
				
				// clica em inserir documento
				driver.switchTo().frame(MyUtils.encontrarElemento(wait, By.id("ifrVisualizacao")));
				WebElement btnIncluirDocumento = null;
				try {
					btnIncluirDocumento = MyUtils.encontrarElemento(wait, By.xpath("//img[@alt = 'Incluir Documento']"));
				} catch (Exception e) {
					MyUtils.appendLogArea(logArea, "Não foi encontrado o botão de incluir documentos no processo " + respostaAGerar.getNumeroProcessoSEI() + ". Verifique se este processo está aberto. Se não estiver, reabra-o e processe novamente.");
					continue;
				}
				btnIncluirDocumento.click();
				
				// clica no tipo de documento
				WebElement btnOpcaoTipoDocumento = MyUtils.encontrarElemento(wait, By.xpath("//a[text() = '" + respostaAGerar.getTipoResposta().getTipoDocumento() + "']"));
				btnOpcaoTipoDocumento.click();
	
				// clica em documento modelo
				WebElement lblDocumentoModelo = MyUtils.encontrarElemento(wait, By.xpath("//label[contains(text(), 'Documento Modelo')]"));
				lblDocumentoModelo.click();
	
				// preenche o código do documento modelo
				WebElement txtCodigoDocumentoModelo = MyUtils.encontrarElemento(wait, By.xpath("//input[@id = 'txtProtocoloDocumentoTextoBase']"));
				txtCodigoDocumentoModelo.sendKeys(respostaAGerar.getTipoResposta().getNumeroDocumentoModelo());
	
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
	
				respostaAGerar.setNumeroDocumentoSEI(driver.getTitle().split(" - ")[1]);
				MyUtils.appendLogArea(logArea, "Nº Documento Gerado: " + respostaAGerar.getNumeroDocumentoSEI());
	
				// encontrar o iframe que contem o corpo do documento a ser editado
				driver.switchTo().defaultContent();
				List<WebElement> frmIFrames = null;
				int espera = 15;
				do {
					TimeUnit.SECONDS.sleep(2);
					frmIFrames = MyUtils.encontrarElementos(wait, By.tagName("iframe"));
				} while (--espera >= 0 && (frmIFrames == null || frmIFrames.size() <= 1));
	
				WebElement welAutor = null;
				
				for (WebElement frmIFrame : frmIFrames) {
					driver.switchTo().frame(frmIFrame);

					try {
						welAutor = MyUtils.encontrarElemento(wait2, By.xpath("//*[contains(text(), '<autor>')]"));
					} catch (Exception e) {
						welAutor = null;
					}
	
					if (welAutor != null) {
						break;
					} else {
						driver.switchTo().defaultContent();
					}
				}
				
				// clica no primeiro paragrafo encontrado no iframe
				welAutor.click();
				TimeUnit.SECONDS.sleep(1);
				
				// volta ao conteúdo default
				driver.switchTo().defaultContent();
				
				// clica no botão localizar
				WebElement btnLocalizar = MyUtils.encontrarElemento(wait, By.xpath("//div[contains(@id, 'cke_txaEditor') and contains(@class, 'cke_detached') and not(contains(@style, 'display: none'))]//a[@title = 'Localizar']"));
				btnLocalizar.click();
	
				// clica na aba substituir
				WebElement tabSubstituir = MyUtils.encontrarElemento(wait, By.xpath("//a[contains(text(), 'Substituir')]"));
				TimeUnit.SECONDS.sleep(1);
				tabSubstituir.click();
	
				Map<String, String> mapaSubstituicoes = obterMapaSubstituicoes(respostaAGerar, superior);
				
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
					// MyUtils.appendLogArea(logArea, "Resultado da substituição de '" + chave + "' por '" + textoSubstituto + "': " + driver.switchTo().alert().getText());
					driver.switchTo().alert().accept();
				}
				
				// clica em fechar
				WebElement btnFechar = MyUtils.encontrarElemento(wait, By.xpath("//span[text() = 'Fechar']"));
				btnFechar.click();
	
				// procura o botão salvar, conferindo que ele esteja habilitado
				WebElement btnSalvar = MyUtils.encontrarElemento(wait, By.xpath("//div[contains(@id, 'cke_txaEditor') and contains(@class, 'cke_detached') and not(contains(@style, 'display: none'))]//a[contains(@title, 'Salvar') and not(@aria-disabled)]"));
				btnSalvar.click();
				
				TimeUnit.MILLISECONDS.sleep(500);
				
				// aguarda até que o botão de salvar esteja novamente desabilitado para fechar a janela
				MyUtils.encontrarElemento(wait, By.xpath("//div[contains(@id, 'cke_txaEditor') and contains(@class, 'cke_detached') and not(contains(@style, 'display: none'))]//a[contains(@title, 'Salvar') and @aria-disabled]"));
				
				driver.close();
				driver.switchTo().window(janelaPrincipal);

				// clica no botão adicionar ao bloco interno
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

				// aguardar que a linha retorno indicando que o registro está inserido no bloco
				MyUtils.encontrarElemento(wait, By.xpath("//table[@id = 'tblDocumentos']/tbody/tr[./td[2]/a[text() = '" + respostaAGerar.getNumeroDocumentoSEI() + "'] and ./td[5]/a[text() = '" + respostaAGerar.getBlocoAssinatura() + "']]"));
				
				driver.switchTo().defaultContent();
				
				// atualiza o número do documento gerado no SEI
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
				return "Não foi encontrado o botão de incluir documentos no processo " + resposta.getNumeroProcessoSEI() + ". Verifique se este processo está aberto. Se não estiver, reabra-o e processe novamente.";
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
		
		resposta.getSolicitacao().setArquivosAnexados(true);
		atualizarArquivosAnexados(resposta);

		return "";
	}

	private Map<String, String> obterMapaSubstituicoes(SolicitacaoResposta resposta, Assinante superior) {
		Map<String, String> retorno = new LinkedHashMap<String, String>();
		String numeroProcesso = resposta.getSolicitacao().getNumeroProcesso();
		if (resposta.getSolicitacao().getOrigem().getOrigemId().equals(Origem.SPUNET_ID) && resposta.getSolicitacao().getNumeroProcesso().length() == 17) {
			// reformata o número do processo para a máscara UUUU.NNNNNN/AAAA-DD
			numeroProcesso = numeroProcesso.substring(0, 5) + "." + numeroProcesso.substring(5, 11) + "/" + numeroProcesso.substring(11, 15) + "-" + numeroProcesso.substring(15); 
		}
		retorno.put("<numero_processo>", numeroProcesso);
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
		retorno.put("<area>", MyUtils.emptyStringIfNull(resposta.getSolicitacao().getArea()).trim().equals("") ? "" : "com área de " + resposta.getSolicitacao().getArea() + ", ");
		retorno.put("<coordenada>", MyUtils.emptyStringIfNull(resposta.getSolicitacao().getCoordenada()).trim().equals("") ? "" : "cuja poligonal possui um dos vértices com coordenada " + resposta.getSolicitacao().getCoordenada() + ", ");
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
				 + "	 , datahoraresposta = datetime('now', 'localtime') "
				 + "	 , numeroprocessosei = '" + resposta.getNumeroProcessoSEI() + "' "
				 + "	 , respostaimpressa = " + (resposta.getTipoResposta().getImprimirResposta() ? "false" : "true")
				 + "	 , blocoassinatura = '" + resposta.getBlocoAssinatura() + "' "
				 + "	 , respostanoblocoassinatura = true "
				 + "     , assinanteidsuperior = " + superior.getAssinanteId()
				 + " where solicitacaorespostaid = " + resposta.getSolicitacaoRespostaId());

		JPAUtils.executeUpdate(conexao, sql.toString());
	}

	private Map<String, List<SolicitacaoResposta>> obterRespostasACadastrar() throws Exception {
		Map<String, List<SolicitacaoResposta>> retorno = new TreeMap<String, List<SolicitacaoResposta>>();

		List<SolicitacaoResposta> respostas = despachoServico.obterRespostasAGerar();
		
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
	
	private String validarPastaProcessoIndividual() throws Exception {
		List<Assinante> assinantes = despachoServico.obterAssinante(null, null, false, true);
		for (Assinante assinante : assinantes) {
			if (assinante.getPastaArquivoProcesso().equals("") || !MyUtils.arquivoExiste(assinante.getPastaArquivoProcesso())) {
				return "A pasta de arquivos de processos individuais para o assinante " + assinante.getNome() + " não existe ou não está configurada: " + assinante.getPastaArquivoProcesso() + "\nConfigure a pasta para o assinante e tente novamente.";
			}
		}
		return "";
	}
}
