import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

import framework.MyButton;
import framework.MyUtils;
import framework.SpringUtilities;

@SuppressWarnings("serial")
public class ValidacaoDadosCadastrados extends JInternalFrame {

	private Process proc;
	private EntityManager conexao;
	private JFileChooser filPasta = new JFileChooser();
	private JButton btnAbrirPasta = new JButton("Selecionar pasta");
	private JLabel lblNomePasta = new JLabel("") {{ setVerticalTextPosition(SwingConstants.TOP); setSize(600, 20); }};
	private JLabel lblPasta = new JLabel("Pasta:", JLabel.TRAILING) {{ setLabelFor(filPasta); }};
	private JFileChooser filAplicativo = new JFileChooser();
	private JButton btnAbrirAplicativo = new JButton("Selecionar aplicativo");
	private JLabel lblNomeAplicativo = new JLabel("") {{ setVerticalTextPosition(SwingConstants.TOP); setSize(600, 20); }};
	private JLabel lblAplicativo = new JLabel("Aplicativo:", JLabel.TRAILING) {{ setLabelFor(filAplicativo); }};
	private JPanel painelDados = new JPanel() {{ setLayout(new SpringLayout()); }};
	private JButton btnProcessar = new JButton("Processar"); 
	private JEditorPane logArea = new JEditorPane() {{ setPreferredSize(new Dimension(1500, 700)); setContentType("text/html"); }};
	private JScrollPane areaDeRolagem = new JScrollPane(logArea) {{ setPreferredSize(new Dimension(1500, 700)); }};
	private SPUNetServico cadastroServico;
	private List<File> arquivosAProcessar;
	private int indiceArquivo = 0;
	private MyButton btnProximo = new MyButton("Pr�ximo");
	private MyButton btnValidar = new MyButton("Validar");
	private MyButton btnRevisar = new MyButton("Revisar");
	private File arquivoSendoProcessado;

	public ValidacaoDadosCadastrados(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		habilitarBotoes();

		this.conexao = conexao;
		cadastroServico = new SPUNetServico(this.conexao);

		lblNomeAplicativo.setText(MyUtils.obterConfiguracaoLocal("aplicativoimagem", ""));

		filAplicativo.setFileFilter(new FileNameExtensionFilter("Aplica��es (*.exe)", "exe"));

		JPanel painelArquivo = new JPanel() {{ add(lblPasta); add(btnAbrirPasta); }};
		JPanel painelAplicativo = new JPanel() {{ add(lblAplicativo); add(btnAbrirAplicativo); }};

		String espacoEmDisco = MyUtils.verificacaoDeEspacoEmDisco(20);
		
		if (espacoEmDisco != null) {
			painelDados.add(new JLabel("<html><font color='red'>" + espacoEmDisco + "</font></html>"));
			painelDados.add(new JPanel());
		}
		painelDados.add(painelArquivo);
		painelDados.add(lblNomePasta);
		painelDados.add(painelAplicativo);
		painelDados.add(lblNomeAplicativo);
		painelDados.add(btnProcessar); 
		painelDados.add(new JPanel()); 

		JPanel painelAcoes = new JPanel() {{ add(btnProximo); add(btnValidar); add(btnRevisar); }};
		
		SpringUtilities.makeCompactGrid(painelDados,
	            espacoEmDisco == null ? 3 : 4, 2, //rows, cols
	            6, 6, //initX, initY
	            6, 6); //xPad, yPad

		add(painelDados, BorderLayout.NORTH);
		add(areaDeRolagem, BorderLayout.CENTER);
		add(painelAcoes, BorderLayout.SOUTH);

		btnProximo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processarArquivo();
			}
		});

		btnValidar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					atualizarStatusArquivo(arquivoSendoProcessado, "Validar");
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Erro ao marcar como 'Validar' o arquivo " + arquivoSendoProcessado.getAbsolutePath() + ":\n" + e1.getMessage());
					e1.printStackTrace();
				}
				processarArquivo();
			}
		});

		btnRevisar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					atualizarStatusArquivo(arquivoSendoProcessado, "Revisar");
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Erro ao marcar como 'Revisar' o arquivo " + arquivoSendoProcessado.getAbsolutePath() + ":\n" + e1.getMessage());
					e1.printStackTrace();
				}
				processarArquivo();
			}
		});

		btnProcessar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String mensagemErro = "";
				if (lblNomePasta.getText().equals("")) {
					mensagemErro += "Para iniciar o processamento � necess�rio selecionar uma pasta para ser processada. \n";
				}

				if (lblNomeAplicativo.getText().equals("")) {
					mensagemErro += "Para iniciar o processamento � necess�rio selecionar um aplicativo de leitura de imagens. \n";
				}

				if (!mensagemErro.equals("")) {
					JOptionPane.showMessageDialog(null, mensagemErro);
					return;
				}

				indiceArquivo = 0;
				arquivosAProcessar = new ArrayList<File>();

				logArea.setText("Aguarde a obten��o da lista de arquivos...");
				
				try {
					obterArquivosAProcessar(lblNomePasta.getText(), arquivosAProcessar);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				logArea.setText("Foram obtidos " + arquivosAProcessar.size() + " para serem processados.");

				if (arquivosAProcessar.size() != 0) {
					processarArquivo();
				}
			}
		});

		btnAbrirPasta.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				filPasta.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				filPasta.setAcceptAllFileFilterUsed(false);
				int retorno = filPasta.showOpenDialog(ValidacaoDadosCadastrados.this);
				if (retorno == JFileChooser.APPROVE_OPTION) {
					if (filPasta.getSelectedFile().exists()) {
						lblNomePasta.setText(filPasta.getSelectedFile().getAbsolutePath());
					}
				}
			}
		});

		btnAbrirAplicativo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int retorno = filAplicativo.showOpenDialog(ValidacaoDadosCadastrados.this);
				if (retorno == JFileChooser.APPROVE_OPTION) {
					if (filAplicativo.getSelectedFile().exists()) {
						lblNomeAplicativo.setText(filAplicativo.getSelectedFile().getAbsolutePath());
						MyUtils.salvarConfiguracaoLocal("aplicativoimagem", lblNomeAplicativo.getText(), "N�o foi poss�vel salvar o aplicativo de imagem");
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

	private void obterArquivosAProcessar(String caminho, List<File> arquivosAProcessar) throws Exception {
		List<File> arquivos = MyUtils.obterArquivos(caminho, true, "tif");
		for (File arquivo : arquivos) {
			if (arquivo.isDirectory()) {
				obterArquivosAProcessar(arquivo.getAbsolutePath(), arquivosAProcessar);
			} else {
				Validacao validacao = MyUtils.entidade(cadastroServico.obterValidacao(null, null, FilenameUtils.removeExtension(arquivo.getName()), null));
				if (validacao == null || validacao.getStatus().equalsIgnoreCase("N�o encontrado") || validacao.getStatus().equalsIgnoreCase("Revisar")) {
					arquivosAProcessar.add(arquivo);
				}
			}
		}
	}

	private void habilitarBotoes() {
		btnProximo.setEnabled(arquivosAProcessar != null && arquivosAProcessar.size() > indiceArquivo);
		btnValidar.setEnabled(arquivosAProcessar != null && arquivosAProcessar.size() > 0);
		btnRevisar.setEnabled(arquivosAProcessar != null && arquivosAProcessar.size() > 0);
	}

	private void processarArquivo() {
		if (indiceArquivo >= arquivosAProcessar.size()) {
			JOptionPane.showMessageDialog(null, "N�o h� mais arquivos a processar!");
			return;
		}
		arquivoSendoProcessado = arquivosAProcessar.get(indiceArquivo);
		indiceArquivo ++;
		if (proc != null) {
			proc.destroy();
		}
		habilitarBotoes();
		try {
			processarArquivo(arquivoSendoProcessado);
		} catch (Exception e) {
			logArea.setText("Erro ao processar o arquivo '" + arquivoSendoProcessado.getAbsolutePath() + "': \n\n" + e.getMessage());
		}
	}

	private void processarArquivo(File fileIn) throws Exception {
		String html = "<h1><b><u>".concat("Arquivo:</u></b>").concat(" ").concat(fileIn.getAbsolutePath()).concat("</h1>");

		Geoinformacao geo = MyUtils.entidade(cadastroServico.obterGeoinformacao(null, true, FilenameUtils.removeExtension(fileIn.getName())));

		if (geo == null) {
			html += "<p><font color='red'>".concat("Registro n�o encontrado na base de dados!").concat("</font></p>");
			atualizarStatusArquivo(fileIn, "N�o encontrado");
			btnValidar.setEnabled(false);
			btnRevisar.setEnabled(false);
		} else {
			html += "<h2>Identifica��o</h2>";
			html += "<p>";
			html += "<tt><b>T�tulo do Produto:</b>".concat(" ").concat(geo.getIdentTituloProduto()).concat("</tt><br>");
			html += "<tt><b>Data de Cria��o..:</b>".concat(" ").concat(geo.getIdentDataCriacao()).concat("</tt><br>");
			html += "<tt><b>Resumo...........:</b>".concat(" ").concat(geo.getIdentResumo()).concat("</tt><br>");
			html += "</p>";
			html += "<br>";
			html += "<h2>Sistema de Refer�ncia</h2>";
			html += "<p>";
			html += "<tt><b>Datum.....:</b>".concat(" ").concat(geo.getSisrefDatum()).concat("</tt><br>");
			html += "<tt><b>Proje��o..:</b>".concat(" ").concat(geo.getSisrefProjecao()).concat("</tt><br>");
			if (!geo.getSisrefObservacao().equals("")) html += "<tt><b>Observa��o:</b>".concat(" ").concat(geo.getSisrefObservacao()).concat("</tt><br>");
			html += "</p>";
			html += "<br>";
			html += "<h2>Identifica��o do CDG</h2>";
			html += "<p>";
			html += "<tt><b>Tipo Repr. Espacial:</b>".concat(" ").concat(geo.getIdentcdgTipoReprEspacial()).concat("</tt><br>");
			html += "<tt><b>Escala.............:</b>".concat(" ").concat(geo.getIdentcdgEscala().trim().equals("") ? "N�o informada" : geo.getIdentcdgEscala()).concat("</tt><br>");
			html += "<tt><b>Categoria..........:</b>".concat(" ").concat(geo.getIdentcdgCategoria()).concat("</tt><br>");
			html += "<tt><b>Munic�pio..........:</b>".concat(" ").concat(geo.getIdentcdgMunicipio()).concat("</tt><br>");
			html += "</p>";
			html += "<br>";
			if (!geo.getQualidadeLinhagem().trim().equals("")) {
				html += "<h2>Qualidade</h2>";
				html += "<p>";
				html += "<tt><b>Linhagem:</b>".concat(" ").concat(geo.getQualidadeLinhagem()).concat("</tt><br>");
				html += "</p>";
				html += "<br>";
			}
			if (!geo.getInfadicCamadaInf().trim().equals("")) {
				html += "<h2>Informa��o Adicional</h2>";
				html += "<p>";
				html += "<tt><b>Camada de Informa��o:</b>".concat(" ").concat(geo.getInfadicCamadaInf()).concat("</tt><br>");
				html += "</p>";
				html += "<br>";
			}
			abrirImagem(fileIn.getAbsolutePath());
		}

		logArea.setText(html);
	}

	private void atualizarStatusArquivo(File arquivo, String status) throws Exception {
		String identTituloProduto = FilenameUtils.removeExtension(arquivo.getName());
		Validacao validacao = MyUtils.entidade(cadastroServico.obterValidacao(null, null, identTituloProduto, null));
		if (validacao == null) {
			validacao = new Validacao();
			validacao.setIdentTituloProduto(identTituloProduto);
			validacao.setNomeArquivo(arquivo.getAbsolutePath());
			validacao.setStatus(status);
		} else {
			if (!validacao.getNomeArquivo().equalsIgnoreCase(arquivo.getAbsolutePath())) {
				JOptionPane.showMessageDialog(null, "ATEN��O: o produto '" + identTituloProduto + "' j� est� na base de dados e inconsistente:\n\n- Arquivo avaliado: " + 
						arquivo.getAbsolutePath() + "\n- Arquivo cadastrado: " + validacao.getNomeArquivo() + "\n\nA an�lise n�o foi gravada.");
				return;
			}
		}

		cadastroServico.gravarEntidade(validacao);
	}
	
	private void abrirImagem(String arquivo) throws Exception {
		String cmd = lblNomeAplicativo.getText() + " \"" + arquivo + "\"";

		Runtime run = Runtime.getRuntime();
		proc = run.exec(cmd);
	}
}
