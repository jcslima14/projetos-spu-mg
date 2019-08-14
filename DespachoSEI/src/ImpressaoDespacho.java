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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import framework.ComboBoxItem;
import framework.MyComboBox;
import framework.MyComboBoxModel;
import framework.MyLabel;
import framework.MyUtils;
import framework.SpringUtilities;

@SuppressWarnings("serial")
public class ImpressaoDespacho extends JInternalFrame {

	private Connection conexao;
	private MyComboBox cbbAssinante = new MyComboBox();
	private MyLabel lblAssinante = new MyLabel("Assinado por");
	private JTextField txtUsuario = new JTextField(15);
	private JLabel lblUsuario = new JLabel("Usuário:") {{ setLabelFor(txtUsuario); }};
	private JPasswordField txtSenha = new JPasswordField(15);
	private JLabel lblSenha = new JLabel("Senha:") {{ setLabelFor(txtSenha); }};
	private JPanel painelDados = new JPanel() {{ setLayout(new SpringLayout()); }};
	private JButton btnProcessar = new JButton("Processar"); 
	private JTextArea logArea = new JTextArea(30, 100);
	private JScrollPane areaDeRolagem = new JScrollPane(logArea);
	private DespachoServico despachoServico;

	public ImpressaoDespacho(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		this.conexao = conexao;
		despachoServico = new DespachoServico(conexao);

		opcoesAssinante();

		painelDados.add(lblAssinante);
		painelDados.add(cbbAssinante);
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
								imprimirDespachoSEI(txtUsuario.getText(), new String(txtSenha.getPassword()), MyUtils.idItemSelecionado(cbbAssinante));;
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

	private void opcoesAssinante() {
		cbbAssinante.setModel(new MyComboBoxModel());
		MyUtils.insereOpcoesComboBox(conexao, cbbAssinante, "select assinanteid, nome from assinante where superior = false order by nome", Arrays.asList(new ComboBoxItem(0, null, "(Todos)")));
	}

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void imprimirDespachoSEI(String usuario, String senha, Integer assinanteId) throws Exception {
        String pastaRespostasImpressas = despachoServico.obterConteudoParametro(Parametro.PASTA_DESPACHOS_SALVOS);
		String diretorioDespachos = obterDiretorioDespachos(pastaRespostasImpressas);

		MyUtils.appendLogArea(logArea, "Iniciando o navegador web...");
		ChromeOptions opcoes = new ChromeOptions();
		opcoes.setExperimentalOption("prefs", new LinkedHashMap<String, Object>() {{ put("download.prompt_for_download", false); put("download.default_directory", diretorioDespachos); }});
		System.setProperty("webdriver.chrome.driver", MyUtils.chromeWebDriverPath());
        WebDriver driver = new ChromeDriver(opcoes);

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
        MyUtils.fecharPopup(driver);

        // selecionar a unidade default
        MyUtils.selecionarUnidade(driver, wait, despachoServico.obterConteudoParametro(Parametro.UNIDADE_PADRAO_SEI));

		Map<String, List<Despacho>> despachosAImprimir = obterDespachos(1, assinanteId);
		for (String numeroProcessoSEI : despachosAImprimir.keySet()) {
			// pesquisa o número do processo
			driver.switchTo().defaultContent();
			WebElement txtPesquisaRapida = MyUtils.encontrarElemento(wait5, By.name("txtPesquisaRapida"));
			txtPesquisaRapida.clear();
			txtPesquisaRapida.sendKeys(numeroProcessoSEI);
			txtPesquisaRapida.sendKeys(Keys.ENTER);

			// clicar em gerar documentos
			driver.switchTo().frame("ifrVisualizacao");
			WebElement btnGerarPDF = MyUtils.encontrarElemento(wait5, By.xpath("//img[@alt = 'Gerar Arquivo PDF do Processo']"));
			btnGerarPDF.click();

			// clicar em selecionar tudo (precisa clicar 2x, pois o primeiro click marca todos (que já estão marcados) e o segundo desmarca tudo)
			WebElement btnDesmarcarTudo = MyUtils.encontrarElemento(wait, By.xpath("//img[@title = 'Selecionar Tudo']"));
			btnDesmarcarTudo.click();
			TimeUnit.SECONDS.sleep(1);
			btnDesmarcarTudo.click();

			for (Despacho despachoAImprimir : despachosAImprimir.get(numeroProcessoSEI)) {
				String numeroProcesso = despachoAImprimir.getNumeroProcesso();
				String numeroDespacho = despachoAImprimir.getNumeroDocumentoSEI();

				MyUtils.appendLogArea(logArea, "Processo: " + numeroProcesso + " - Nº Despacho: " + numeroDespacho);
	
				// encontra e marca o checkbox do documento
				WebElement chkSelecionarDespacho = null;
				try {
					chkSelecionarDespacho = MyUtils.encontrarElemento(wait5, By.xpath("//tr[./*/a[text() = '" + numeroDespacho + "']]/*/input[@class = 'infraCheckbox']"));
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (chkSelecionarDespacho != null) {
					// trecho para verificar se o despacho possui duas assinaturas
					String janelaAtual = driver.getWindowHandle();
	
					WebElement lnkDespacho = MyUtils.encontrarElemento(wait5, By.xpath("//a[text() = '" + numeroDespacho + "']"));
					lnkDespacho.click();
					
					for (String janelaAberta : driver.getWindowHandles()) {
						driver.switchTo().window(janelaAberta);
					}

					// espera encontrar o fim do documento para verificar se a quantidade de assinaturas está correta
					MyUtils.encontrarElemento(wait, By.xpath("//p[contains(text(), 'Processo nº " + numeroProcessoSEI + "')]"));
					
					List<WebElement> assinaturas = MyUtils.encontrarElementos(wait5, By.xpath("//p[contains(text(), 'Documento assinado eletronicamente por')]"));
	
					driver.close();
					driver.switchTo().window(janelaAtual);
					driver.switchTo().defaultContent();
					driver.switchTo().frame("ifrVisualizacao");
	
					if (assinaturas.size() != 2) {
						MyUtils.appendLogArea(logArea, "Para ser impresso, o documento precisa de 2 assinaturas. Este documento possui " + assinaturas.size() + " assinaturas.");
					} else {
						chkSelecionarDespacho.click();
	
						// apaga arquivo com o nome do processo, caso já exista
						apagarArquivoProcesso(diretorioDespachos, numeroProcessoSEI);
	
						// gera o arquivo no diretório de downloads
						WebElement btnGerarDocumento = MyUtils.encontrarElemento(wait5, By.name("btnGerar"));
						btnGerarDocumento.click();
	
						renomearArquivoProcesso(diretorioDespachos, numeroProcessoSEI, pastaRespostasImpressas + "\\" + numeroProcesso + ".pdf");
	
						// verifica se o arquivo foi gerado e atualiza o número do despacho gerado no SEI
						atualizarDespachoGerado(despachoAImprimir);
	
						chkSelecionarDespacho = MyUtils.encontrarElemento(wait5, By.xpath("//tr[./*/a[text() = '" + numeroDespacho + "']]/*/input[@class = 'infraCheckbox']"));
						chkSelecionarDespacho.click();
					}
				} else {
					MyUtils.appendLogArea(logArea, "Despacho não encontrado ou não habilitado para geração em PDF");
				}
			} // fim do loop de leitura dos despachos de cada processo
		} // fim do loop de diferentes processos com despachos

		// início da retirada dos despachos do bloco de assinatura
		MyUtils.appendLogArea(logArea, "Preparando para retirar despachos dos blocos de assinatura...");

		Map<String, List<Despacho>> despachosARetirar = obterDespachos(2, assinanteId);
		for (String blocoAssinatura : despachosARetirar.keySet()) {
			driver.switchTo().defaultContent();
			WebElement btnControleProcessos = MyUtils.encontrarElemento(wait5, By.id("lnkControleProcessos"));
			btnControleProcessos.click();

			WebElement btnBlocosAssinatura = MyUtils.encontrarElemento(wait5, By.xpath("//a[text() = 'Blocos de Assinatura']"));
			btnBlocosAssinatura.click();

			WebElement lnkBlocoAssinatura = null;
			
			try {
				lnkBlocoAssinatura = MyUtils.encontrarElemento(wait5, By.xpath("//table[@summary = 'Tabela de Blocos.']/tbody/tr/td/a[text() = '" + blocoAssinatura + "']"));
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (lnkBlocoAssinatura == null) {
				MyUtils.appendLogArea(logArea, "*** Não foi possível encontrar o bloco de assinatura " + blocoAssinatura + ".");
				continue;
			}

			lnkBlocoAssinatura.click();
			List<Despacho> despachosRetirados = new ArrayList<Despacho>();

			for (Despacho despachoARetirar : despachosARetirar.get(blocoAssinatura)) {
				WebElement chkSelecaoLinha = null;
				try {
					chkSelecaoLinha = MyUtils.encontrarElemento(wait5, By.xpath("//table[@summary = 'Tabela de Processos/Documentos.']/tbody/tr[.//*[text() = '" + despachoARetirar.getNumeroDocumentoSEI() + "']]/td/input"));
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (chkSelecaoLinha != null) {
					MyUtils.appendLogArea(logArea, "Marcando para retirada o despacho " + despachoARetirar.getNumeroDocumentoSEI());
					chkSelecaoLinha.click();
					despachosRetirados.add(despachoARetirar);
				}
			}
			
			if (despachosRetirados.size() > 0) {
				MyUtils.appendLogArea(logArea, "Retirando os despachos marcados...");
				WebElement btnExcluir = MyUtils.encontrarElemento(wait5, By.id("btnExcluir"));
				btnExcluir.click();
				TimeUnit.MILLISECONDS.sleep(500);
				driver.switchTo().alert().accept();
				TimeUnit.SECONDS.sleep(2);

				MyUtils.encontrarElemento(wait5, By.id("btnFechar"));

				MyUtils.appendLogArea(logArea, "Atualizando a situação dos despachos retirados...");
				for (Despacho despachoRetirado : despachosRetirados) {
					atualizarDespachoRetiradoBlocoAssinatura(despachoRetirado);
				}
			}
		}
		
		MyUtils.appendLogArea(logArea, "Fim do Processamento...");

        driver.close();
        driver.quit();
	}

	private void apagarArquivoProcesso(String diretorioDespachos, String numeroProcessoSEI) {
		File arquivo = new File(diretorioDespachos + "\\" + "SEI_" + numeroProcessoSEI.replace("/", "_").replace("-", "_") + ".pdf");
		if (arquivo.exists()) {
			arquivo.delete();
		}
	}

	private void renomearArquivoProcesso(String diretorioDespachos, String numeroProcessoSEI, String arquivoRenomeado) throws Exception {
		int vezes = 0;
		while (vezes++ < 15) {
			TimeUnit.SECONDS.sleep(1);
			File arquivo = new File(diretorioDespachos + "\\" + "SEI_" + numeroProcessoSEI.replace("/", "_").replace("-", "_") + ".pdf");
			if (arquivo.exists() && arquivo.length() > 0) {
				File novoArquivo = new File(arquivoRenomeado);
				if (novoArquivo.exists()) novoArquivo.delete();
				arquivo.renameTo(novoArquivo);
				break;
			}
		};
	}

	private String obterDiretorioDespachos(String pastaRespostas) throws Exception {
		File caminho = new File(pastaRespostas);
		return caminho.getParent();
	}

	private void atualizarDespachoGerado(Despacho despacho) throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("update despacho"
				 + "   set despachoimpresso = true "
				 + "	 , datahoraimpressao = datetime('now', 'localtime') "
				 + " where despachoid = " + despacho.getDespachoId());

		MyUtils.execute(conexao, sql.toString());
	}

	private void atualizarDespachoRetiradoBlocoAssinatura(Despacho despacho) throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("update despacho"
				 + "   set despachonoblocoassinatura = false "
				 + " where despachoid = " + despacho.getDespachoId());

		MyUtils.execute(conexao, sql.toString());
	}

	// método para obter despachos: tipos de filtro: 1 - despachos não impressos; 2 - despachos a serem retirados do bloco de assinatura
	private Map<String, List<Despacho>> obterDespachos(int tipoFiltro, Integer assinanteId) throws Exception {
		Map<String, List<Despacho>> retorno = new LinkedHashMap<String, List<Despacho>>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select dp.* ");
		sql.append("  from despacho dp ");
		sql.append(" inner join destino dt using (destinoid) ");
		sql.append(" inner join tipodespacho td using (tipodespachoid) ");
		sql.append(" where coalesce(dp.numerodocumentosei, '') <> '' ");
		sql.append("   and coalesce(dp.numeroprocessosei, '') <> '' ");
		sql.append("   and td.imprimirresposta = true ");
		if (assinanteId != null && !assinanteId.equals(0)) {
			sql.append(" and assinanteid = " + assinanteId);
		}
		if (tipoFiltro == 1) {
			sql.append(" and not dp.despachoimpresso ");
		} else if (tipoFiltro == 2) {
			sql.append(" and dp.despachoimpresso ");
			sql.append(" and coalesce(dp.blocoassinatura, '') <> '' ");
			sql.append(" and dp.despachonoblocoassinatura ");
		}

		if (tipoFiltro == 1) {
			sql.append(" order by numeroprocessosei, numerodocumentosei ");
		} else if (tipoFiltro == 2) {
			sql.append(" order by blocoassinatura, numerodocumentosei ");
		}

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			String chave = (tipoFiltro == 1 ? rs.getString("numeroprocessosei") : rs.getString("blocoassinatura"));
			if (retorno.get(chave) == null) retorno.put(chave, new ArrayList<Despacho>());
			retorno.get(chave).add(new Despacho(rs.getInt("despachoid"),
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
