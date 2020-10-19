package views.robo;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
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

import framework.services.SEIService;
import framework.utils.MyUtils;
import framework.utils.SpringUtilities;
import model.Parametro;
import services.DespachoServico;

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
		
		SEIService seiServico = new SEIService("chrome", despachoServico.obterConteudoParametro(Parametro.ENDERECO_SEI));
		
        // acessa o SEI, realiza o login e seleciona unidade padrão
        seiServico.login(usuario, senha, despachoServico.obterConteudoParametro(Parametro.ORGAO_LOGIN_SEI));

		// pesquisa o processo onde deverão ser incluídos os ofícios
        seiServico.pesquisarProcesso(numeroProcesso);

		Map<String, Oficio> oficiosAGerar = obterDadosOficios(lblNomeArquivo.getText());
		for (Oficio oficio : oficiosAGerar.values()) {
			MyUtils.appendLogArea(logArea, "Gerando ofício para UG: " + oficio.ugResponsavel);

			// insere um novo documento
			String numeroDocumentoSEIGerado = seiServico.inserirDocumentoNoProcesso(numeroProcesso, "Ofício", numeroDocumentoModelo);
			MyUtils.appendLogArea(logArea, "Nº Documento Gerado: " + numeroDocumentoSEIGerado);

			// alterna para o frame de destinatário para substituir os dados e clica no primeiro elemento p para mudar o foco
			TimeUnit.SECONDS.sleep(1);
			seiServico.alterarParaFrame(3);
			seiServico.encontrarElemento(By.xpath("(//p)[1]")).click();
			TimeUnit.SECONDS.sleep(1);

			seiServico.substituirMarcacaoDocumento(oficio.mapaSubstituicoesCabecalho());

			// alterna para o frame do corpo do documento para promover as substituições e clica no primeiro elemento p para mudar o foco
			seiServico.alterarParaFrame(4);
			seiServico.encontrarElemento(By.xpath("(//p)[1]")).click();
			TimeUnit.SECONDS.sleep(1);
			
			// obtem a linha da tabela que possui os marcadores a serem substituídos e armazena em string para servir de template para novas linhas a serem adicionadas
			String imovelTemplate = seiServico.encontrarElemento(By.xpath("//table[@id = 'tabela-imoveis-vistoria']/tbody/tr[./td]")).getAttribute("outerHTML");

			// exclui a linha de template para, em seguida, adicionar as linhas com os dados reais
			seiServico.executarJavaScript("document.querySelector('#tabela-imoveis-vistoria > tbody').removeChild(document.querySelector('#tabela-imoveis-vistoria > tbody').lastChild)");
			TimeUnit.MILLISECONDS.sleep(500);
			
			String novosImoveis = "";
			
			// adicionar linhas à tabela de imóveis a vistoriar
			for (Imovel imovel : oficio.listaImoveis) {
				novosImoveis += imovel.substituirMarcadores(imovelTemplate);
				TimeUnit.MILLISECONDS.sleep(500);
			}

			// adiciona os novos imóveis à tabela
			seiServico.executarJavaScript("document.querySelector('#tabela-imoveis-vistoria > tbody').innerHTML += '" + novosImoveis + "'");
			TimeUnit.MILLISECONDS.sleep(500);

			// substitui os marcadores do corpo
			seiServico.substituirMarcacaoDocumento(oficio.mapaSubstituicoesCorpo());

			// salvar documento
			seiServico.salvarFecharDocumento();
			seiServico.incluirDocumentoBlocoAssinatura(numeroDocumentoSEIGerado, blocoAssinatura);
		} // fim do loop de todas as respostas a gerar

		MyUtils.appendLogArea(logArea, "Fim do Processamento...");

		seiServico.fechaNavegador();
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
}
