import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

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
	private JPanel painelDados = new JPanel() {{ setLayout(new SpringLayout()); }};
	private JButton btnProcessar = new JButton("Processar"); 
	private SPUNetServico cadastroServico;
	private List<File> arquivosAProcessar;
	private int indiceArquivo = 0;
	private MyButton btnProximo = new MyButton("Próximo");
	private MyButton btnValidar = new MyButton("Validar");
	private MyButton btnRedigitalizar = new MyButton("Redigitalizar");
	private File arquivoSendoProcessado;
	private Geoinformacao geoSendoProcessada;
	private JComboBox<String> cbbFiltro = new JComboBox<String>();
	private JLabel lblFiltro = new JLabel("Filtro:", JLabel.TRAILING) {{ setLabelFor(cbbFiltro); }};
	private Desktop dt = Desktop.getDesktop();

	private JPanel painelCentral = new JPanel() {{ setLayout(new SpringLayout()); }};
	private JLabel lblNomeArquivo = new JLabel("<html><h2>Arquivo:</h2></html>");
	private JLabel lblIdentTituloProduto = new JLabel("<html><b><u>Título do Produto:</u></b></html>");
	private JTextField txtIdentDataCriacao = new JTextField();
	private JLabel lblIdentDataCriacao = new JLabel("Data de Criação") {{ setLabelFor(txtIdentDataCriacao); }};
	private JTextField txtIdentResumo = new JTextField();
	private JLabel lblIdentResumo = new JLabel("Resumo") {{ setLabelFor(txtIdentResumo); }};
	private JComboBox<String> cbbSisrefDatum = new JComboBox<String>();
	private JLabel lblSisrefDatum = new JLabel("Datum") {{ setLabelFor(cbbSisrefDatum); }};
	private JComboBox<String> cbbSisrefProjecao = new JComboBox<String>();
	private JLabel lblSisrefProjecao = new JLabel("Projeção") {{ setLabelFor(cbbSisrefProjecao); }};
	private JTextField txtSisrefObservacao = new JTextField();
	private JLabel lblSisrefObservacao = new JLabel("Observação") {{ setLabelFor(txtSisrefObservacao); }};
	private JComboBox<String> cbbIdentcdgTipoReprEspacial = new JComboBox<String>();
	private JLabel lblIdentcdgTipoReprEspacial = new JLabel("Tipo de Representação Espacial") {{ setLabelFor(cbbIdentcdgTipoReprEspacial); }};
	private JTextField txtIdentcdgEscala = new JTextField();
	private JLabel lblIdentcdgEscala = new JLabel("Escala") {{ setLabelFor(txtIdentcdgEscala); }};
	private JComboBox<String> cbbIdentcdgCategoria = new JComboBox<String>();
	private JLabel lblIdentcdgCategoria = new JLabel("Categoria") {{ setLabelFor(cbbIdentcdgCategoria); }};
	private JComboBox<String> cbbIdentcdgMunicipio = new JComboBox<String>();
	private JLabel lblIdentcdgMunicipio = new JLabel("Município") {{ setLabelFor(cbbIdentcdgMunicipio); }};
	private JTextField txtQualidadeLinhagem = new JTextField();
	private JLabel lblQualidadeLinhagem = new JLabel("Linhagem") {{ setLabelFor(txtQualidadeLinhagem); }};
	private JComboBox<String> cbbInfadicCamadaInf = new JComboBox<String>();
	private JLabel lblInfadicCamadaInf = new JLabel("Camada de Informação") {{ setLabelFor(cbbInfadicCamadaInf); }};
	private JCheckBox chkGeorreferenciavel = new JCheckBox("Georreferenciável?", false);

	public ValidacaoDadosCadastrados(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		habilitarProximo();
		habilitarObjetos(false);

		opcoesFiltro();
		
		this.conexao = conexao;
		cadastroServico = new SPUNetServico(this.conexao);

		JPanel painelArquivo = new JPanel() {{ add(lblPasta); add(btnAbrirPasta); }};

		JPanel painelCentralPai = new JPanel();
		painelCentral.setAlignmentX(LEFT_ALIGNMENT);
		painelCentralPai.setLayout(new BoxLayout(painelCentralPai, BoxLayout.Y_AXIS));
		painelCentralPai.setAlignmentX(LEFT_ALIGNMENT);
		painelCentralPai.add(lblNomeArquivo);
		painelCentralPai.add(painelCentral);
		
//		//
//		painelCentral.add(lblNomeArquivo);
//		painelCentral.add(new JPanel());
//		painelCentral.add(new JPanel());
//		painelCentral.add(new JPanel());
//		painelCentral.add(new JPanel());
		//
		painelCentral.add(new JLabel("<html><h3>Identificação</h3></html>"));
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		//
		painelCentral.add(lblIdentTituloProduto);
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		//
		painelCentral.add(lblIdentDataCriacao);
		painelCentral.add(txtIdentDataCriacao);
		painelCentral.add(new JPanel());
		painelCentral.add(lblIdentResumo);
		painelCentral.add(txtIdentResumo);
		//
		painelCentral.add(new JLabel("<html><h3>Sistema de Referência</h3></html>"));
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		//
		painelCentral.add(lblSisrefDatum);
		painelCentral.add(cbbSisrefDatum);
		painelCentral.add(new JPanel());
		painelCentral.add(lblSisrefProjecao);
		painelCentral.add(cbbSisrefProjecao);
		//
		painelCentral.add(lblSisrefObservacao);
		painelCentral.add(txtSisrefObservacao);
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		//
		painelCentral.add(new JLabel("<html><h3>Indentificação CDG</h3></html>"));
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		//
		painelCentral.add(lblIdentcdgTipoReprEspacial);
		painelCentral.add(cbbIdentcdgTipoReprEspacial);
		painelCentral.add(new JPanel());
		painelCentral.add(lblIdentcdgEscala);
		painelCentral.add(txtIdentcdgEscala);
		//
		painelCentral.add(lblIdentcdgCategoria);
		painelCentral.add(cbbIdentcdgCategoria);
		painelCentral.add(new JPanel());
		painelCentral.add(lblIdentcdgMunicipio);
		painelCentral.add(cbbIdentcdgMunicipio);
		//
		painelCentral.add(new JLabel("<html><h3>Qualidade</h3></html>"));
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		//
		painelCentral.add(lblQualidadeLinhagem);
		painelCentral.add(txtQualidadeLinhagem);
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		//
		painelCentral.add(new JLabel("<html><h3>Informações Adicionais</h3></html>"));
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		painelCentral.add(new JPanel());
		//
		painelCentral.add(lblInfadicCamadaInf);
		painelCentral.add(cbbInfadicCamadaInf);
		painelCentral.add(new JPanel());
		painelCentral.add(chkGeorreferenciavel);
		painelCentral.add(new JPanel());

		SpringUtilities.makeCompactGrid(painelCentral,
	            13, 5, //rows, cols
	            6, 6, //initX, initY
	            6, 6); //xPad, yPad

		String espacoEmDisco = MyUtils.verificacaoDeEspacoEmDisco(20);
		
		if (espacoEmDisco != null) {
			painelDados.add(new JLabel("<html><font color='red'>" + espacoEmDisco + "</font></html>"));
			painelDados.add(new JPanel());
		}
		painelDados.add(painelArquivo);
		painelDados.add(lblNomePasta);
		painelDados.add(lblFiltro);
		painelDados.add(cbbFiltro);
		painelDados.add(btnProcessar); 
		painelDados.add(new JPanel()); 

		JPanel painelAcoes = new JPanel() {{ add(btnProximo); add(btnValidar); add(btnRedigitalizar); }};

		SpringUtilities.makeCompactGrid(painelDados,
	            espacoEmDisco == null ? 3 : 4, 2, //rows, cols
	            6, 6, //initX, initY
	            6, 6); //xPad, yPad

		add(painelDados, BorderLayout.NORTH);
		add(painelCentralPai, BorderLayout.CENTER);
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
					if (validarDados()) {
						atualizarStatusArquivo(arquivoSendoProcessado, "Validar");
						processarArquivo();
					}
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Erro ao marcar como 'Validar' o arquivo " + arquivoSendoProcessado.getAbsolutePath() + ":\n" + e1.getMessage());
					e1.printStackTrace();
				}
			}
		});

		btnRedigitalizar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					atualizarStatusArquivo(arquivoSendoProcessado, "Redigitalizar");
					processarArquivo();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Erro ao marcar como 'Redigitalizar' o arquivo " + arquivoSendoProcessado.getAbsolutePath() + ":\n" + e1.getMessage());
					e1.printStackTrace();
				}
			}
		});

		btnProcessar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String mensagemErro = "";
				if (lblNomePasta.getText().equals("")) {
					mensagemErro += "Para iniciar o processamento é necessário selecionar uma pasta para ser processada. \n";
				}

				if (!mensagemErro.equals("")) {
					JOptionPane.showMessageDialog(null, mensagemErro);
					return;
				}

				indiceArquivo = 0;
				arquivosAProcessar = new ArrayList<File>();

				lblNomeArquivo.setText("<html><h2>Aguarde a obtenção da lista de arquivos...</h2></html>");

				try {
					obterArquivosAProcessar(lblNomePasta.getText(), arquivosAProcessar);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				if (arquivosAProcessar.size() != 0) {
					processarArquivo();
				} else {
					lblNomeArquivo.setText("<html><h2><font color='red'>A pesquisa solicitado não encontrou nenhum arquivo...</font></h2></html>");
					habilitarProximo();
					habilitarObjetos(false);
				}
			}
		});

		btnAbrirPasta.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String diretorioPadrao = MyUtils.obterConfiguracaoLocal("ultimapastavalidacao", null);
				if (diretorioPadrao != null && !diretorioPadrao.trim().equals("")) {
					File dirPadrao = new File(diretorioPadrao);
					if (dirPadrao.exists()) {
						filPasta.setCurrentDirectory(dirPadrao);
					}
				}
				filPasta.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				filPasta.setAcceptAllFileFilterUsed(false);
				int retorno = filPasta.showOpenDialog(ValidacaoDadosCadastrados.this);
				if (retorno == JFileChooser.APPROVE_OPTION) {
					if (filPasta.getSelectedFile().exists()) {
						lblNomePasta.setText(filPasta.getSelectedFile().getAbsolutePath());
						MyUtils.salvarConfiguracaoLocal("ultimapastavalidacao", lblNomePasta.getText(), null);
					}
				}
			}
		});
	}

	private void opcoesFiltro() {
		cbbFiltro.addItem("(Somente arquivos não analisados)");
		cbbFiltro.addItem("Não encontrado");
		cbbFiltro.addItem("Redigitalizar");
		cbbFiltro.setSelectedIndex(0);
	}

	private void opcoesSisrefDatum(String opcaoSelecionada) {
		if (cbbSisrefDatum.getItemCount() == 0) {
			cbbSisrefDatum.addItem("Córrego Alegre");
			cbbSisrefDatum.addItem("SAD 69");
			cbbSisrefDatum.addItem("SAD 69/96");
			cbbSisrefDatum.addItem("Sem Datum");
			cbbSisrefDatum.addItem("SIRGAS 2000");
			cbbSisrefDatum.addItem("Sistema de Coordenadas Local");
			cbbSisrefDatum.addItem("WGS 84");
		}

		if (opcaoSelecionada != null && !opcaoSelecionada.equals("")) {
			cbbSisrefDatum.setSelectedItem(opcaoSelecionada);
		}
	}

	private void opcoesSisrefProjecao(String opcaoSelecionada) {
		if (cbbSisrefProjecao.getItemCount() == 0) {
			cbbSisrefProjecao.addItem("Coordenadas geográficas (não projetado)");
			cbbSisrefProjecao.addItem("UTM Zona 18N");
			cbbSisrefProjecao.addItem("UTM Zona 18S");
			cbbSisrefProjecao.addItem("UTM Zona 19N");
			cbbSisrefProjecao.addItem("UTM Zona 19S");
			cbbSisrefProjecao.addItem("UTM Zona 20N");
			cbbSisrefProjecao.addItem("UTM Zona 20S");
			cbbSisrefProjecao.addItem("UTM Zona 21N");
			cbbSisrefProjecao.addItem("UTM Zona 21S");
			cbbSisrefProjecao.addItem("UTM Zona 22N");
			cbbSisrefProjecao.addItem("UTM Zona 22S");
			cbbSisrefProjecao.addItem("UTM Zona 23S");
			cbbSisrefProjecao.addItem("UTM Zona 24S");
			cbbSisrefProjecao.addItem("UTM Zona 25S");
		}

		if (opcaoSelecionada != null && !opcaoSelecionada.equals("")) {
			cbbSisrefProjecao.setSelectedItem(opcaoSelecionada);
		}
	}
	
	private void opcoesIdentcdgTipoReprEspacial(String opcaoSelecionada) {
		if (cbbIdentcdgTipoReprEspacial.getItemCount() == 0) {
			cbbIdentcdgTipoReprEspacial.addItem("Matricial");
			cbbIdentcdgTipoReprEspacial.addItem("Modelo Estereoscópio");
			cbbIdentcdgTipoReprEspacial.addItem("Texto/Tabela");
			cbbIdentcdgTipoReprEspacial.addItem("TIN");
			cbbIdentcdgTipoReprEspacial.addItem("Vetorial");
			cbbIdentcdgTipoReprEspacial.addItem("Vídeo");
		}

		if (opcaoSelecionada != null && !opcaoSelecionada.equals("")) {
			cbbIdentcdgTipoReprEspacial.setSelectedItem(opcaoSelecionada);
		}
	}
	
	private void opcoesIdentcdgCategoria(String opcaoSelecionada) {
		if (cbbIdentcdgCategoria.getItemCount() == 0) {
			cbbIdentcdgCategoria.addItem("Agricultura");
			cbbIdentcdgCategoria.addItem("Agricultura e Pecuária");
			cbbIdentcdgCategoria.addItem("Águas Interiores");
			cbbIdentcdgCategoria.addItem("Altimetria e Batimetria");
			cbbIdentcdgCategoria.addItem("Ambiente");
			cbbIdentcdgCategoria.addItem("Área Protegida");
			cbbIdentcdgCategoria.addItem("Biomas");
			cbbIdentcdgCategoria.addItem("Biótopos");
			cbbIdentcdgCategoria.addItem("Cartografia de Base Coberturas");
			cbbIdentcdgCategoria.addItem("Clima Meteorologia");
			cbbIdentcdgCategoria.addItem("Climatologia Atmosfera");
			cbbIdentcdgCategoria.addItem("Concessões e Comunicação");
			cbbIdentcdgCategoria.addItem("Cultura");
			cbbIdentcdgCategoria.addItem("Defesa");
			cbbIdentcdgCategoria.addItem("Economia");
			cbbIdentcdgCategoria.addItem("Educação");
			cbbIdentcdgCategoria.addItem("Elevação (altimetria e batimetria)");
			cbbIdentcdgCategoria.addItem("Energia");
			cbbIdentcdgCategoria.addItem("Especificações e Metodologias");
			cbbIdentcdgCategoria.addItem("Esportes e Lazer");
			cbbIdentcdgCategoria.addItem("Fauna e Flora");
			cbbIdentcdgCategoria.addItem("Geociências");
			cbbIdentcdgCategoria.addItem("Geografia");
			cbbIdentcdgCategoria.addItem("Geologia Recursos Minerais");
			cbbIdentcdgCategoria.addItem("Geomorfologia (Relevo)");
			cbbIdentcdgCategoria.addItem("Habitação");
			cbbIdentcdgCategoria.addItem("Hidrografia e Hidrologia");
			cbbIdentcdgCategoria.addItem("Imageamento Ortoimagem");
			cbbIdentcdgCategoria.addItem("Informação Militar");
			cbbIdentcdgCategoria.addItem("Limites Administrativos");
			cbbIdentcdgCategoria.addItem("Limites Político Administrativos");
			cbbIdentcdgCategoria.addItem("Localização");
			cbbIdentcdgCategoria.addItem("Mapeamento Aeronáutico");
			cbbIdentcdgCategoria.addItem("Mapeamento Básico Geográfico");
			cbbIdentcdgCategoria.addItem("Mapeamento Básico Topográfico");
			cbbIdentcdgCategoria.addItem("Mapeamento Básico Cadastral");
			cbbIdentcdgCategoria.addItem("Mapeamento Náutico");
			cbbIdentcdgCategoria.addItem("Mapeamento Fundiário");
			cbbIdentcdgCategoria.addItem("Monitoramento Ambiental");
			cbbIdentcdgCategoria.addItem("Nomes Geográficos");
			cbbIdentcdgCategoria.addItem("Normas");
			cbbIdentcdgCategoria.addItem("Oceanos");
			cbbIdentcdgCategoria.addItem("Patrimônio Edificado");
			cbbIdentcdgCategoria.addItem("Pesca e Aquicultura");
			cbbIdentcdgCategoria.addItem("Pesca e Pecuária");
			cbbIdentcdgCategoria.addItem("Planejamento e Cadastro");
			cbbIdentcdgCategoria.addItem("Redes Geodésicas");
			cbbIdentcdgCategoria.addItem("Saneamento");
			cbbIdentcdgCategoria.addItem("Saúde");
			cbbIdentcdgCategoria.addItem("Serviços Concessionados");
			cbbIdentcdgCategoria.addItem("Sociedade e Cultura");
			cbbIdentcdgCategoria.addItem("Socioeconomia");
			cbbIdentcdgCategoria.addItem("Solos");
			cbbIdentcdgCategoria.addItem("Transporte");
			cbbIdentcdgCategoria.addItem("Transportes");
			cbbIdentcdgCategoria.addItem("Vegetação");
		}

		if (opcaoSelecionada != null && !opcaoSelecionada.equals("")) {
			cbbIdentcdgCategoria.setSelectedItem(opcaoSelecionada);
		}
	}

	private void opcoesIdentcdgMunicipio(String opcaoSelecionada) throws Exception {
		if (cbbIdentcdgMunicipio.getItemCount() == 0) {
			for (Municipio municipio : cadastroServico.obterMunicipio(null, null)) {
				cbbIdentcdgMunicipio.addItem(municipio.getNome());
			}
		}

		if (opcaoSelecionada != null && !opcaoSelecionada.equals("")) {
			cbbIdentcdgMunicipio.setSelectedItem(opcaoSelecionada);
		}
	}

	private void opcoesInfadicCamadaInf(String opcaoSelecionada) {
		if (cbbInfadicCamadaInf.getItemCount() == 0) {
			cbbInfadicCamadaInf.addItem("Energia e Comunicação");
			cbbInfadicCamadaInf.addItem("Geodésica/Topográfica");
			cbbInfadicCamadaInf.addItem("Hidrografia");
			cbbInfadicCamadaInf.addItem("Imóvel");
			cbbInfadicCamadaInf.addItem("Limite Patrimônio Público Federal");
			cbbInfadicCamadaInf.addItem("Limite Político Administrativo");
			cbbInfadicCamadaInf.addItem("Mobiliário Urbano");
			cbbInfadicCamadaInf.addItem("Pontos de Referência");
			cbbInfadicCamadaInf.addItem("Relevo");
			cbbInfadicCamadaInf.addItem("Sistema de Transporte");
			cbbInfadicCamadaInf.addItem("Vegetação");
		}

		if (opcaoSelecionada != null && !opcaoSelecionada.equals("")) {
			cbbInfadicCamadaInf.setSelectedItem(opcaoSelecionada);
		}
	}

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setSize(1500, 800);
		// this.pack();
		this.setVisible(true);
		this.show();
	}

	private void obterArquivosAProcessar(String caminho, List<File> arquivosAProcessar) throws Exception {
		String filtro = null;
		if (cbbFiltro.getSelectedIndex() != 0) {
			filtro = cbbFiltro.getSelectedItem().toString();
		}
		List<File> arquivos = MyUtils.obterArquivos(caminho, true, "tif", "tiff");
		for (File arquivo : arquivos) {
			if (arquivo.isDirectory()) {
				obterArquivosAProcessar(arquivo.getAbsolutePath(), arquivosAProcessar);
			} else {
				Validacao validacao = MyUtils.entidade(cadastroServico.obterValidacao(null, null, FilenameUtils.removeExtension(arquivo.getName()), null));
				if (filtro == null && validacao != null) continue;
				if (filtro != null && validacao != null && !validacao.getStatus().equalsIgnoreCase(filtro)) continue;
				arquivosAProcessar.add(arquivo);
			}
		}
	}

	private void habilitarObjetos(boolean habilitado) {
		txtIdentDataCriacao.setEnabled(habilitado);
		txtIdentResumo.setEnabled(habilitado);
		cbbSisrefDatum.setEnabled(habilitado);
		cbbSisrefProjecao.setEnabled(habilitado);
		txtSisrefObservacao.setEnabled(habilitado);
		cbbIdentcdgTipoReprEspacial.setEnabled(habilitado);
		txtIdentcdgEscala.setEnabled(habilitado);
		cbbIdentcdgCategoria.setEnabled(habilitado);
		cbbIdentcdgMunicipio.setEnabled(habilitado);
		txtQualidadeLinhagem.setEnabled(habilitado);
		cbbInfadicCamadaInf.setEnabled(habilitado);
		chkGeorreferenciavel.setEnabled(habilitado);
		btnValidar.setEnabled(habilitado && arquivosAProcessar != null && arquivosAProcessar.size() > 0);
		btnRedigitalizar.setEnabled(habilitado && arquivosAProcessar != null && arquivosAProcessar.size() > 0);
	}
	
	private void habilitarProximo() {
		btnProximo.setEnabled(arquivosAProcessar != null && arquivosAProcessar.size() > indiceArquivo);
	}

	private void processarArquivo() {
		if (indiceArquivo >= arquivosAProcessar.size()) {
			JOptionPane.showMessageDialog(null, "Não há mais arquivos a processar!");
			habilitarObjetos(false);
			return;
		}
		arquivoSendoProcessado = arquivosAProcessar.get(indiceArquivo);
		indiceArquivo ++;
		if (proc != null) {
			proc.destroy();
		}
		try {
			processarArquivo(arquivoSendoProcessado);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao processar o arquivo '" + arquivoSendoProcessado.getAbsolutePath() + "': \n\n" + e.getMessage());
		}
	}

	private void processarArquivo(File fileIn) throws Exception {
		lblNomeArquivo.setText("<html><h2><b><u>".concat("Arquivo (" + indiceArquivo + "/" + arquivosAProcessar.size() + "):</u></b>").concat(" ").concat(fileIn.getAbsolutePath()).concat("</h1></html>"));

		geoSendoProcessada = MyUtils.entidade(cadastroServico.obterGeoinformacao(null, true, FilenameUtils.removeExtension(fileIn.getName())));
		habilitarProximo();
		
		if (geoSendoProcessada == null) {
			lblIdentTituloProduto.setText("<html><b><font color='red'>Registro não encontrado na base de dados!</font></b></html>");
			atualizarStatusArquivo(fileIn, "Não encontrado");
			habilitarObjetos(false);
			geoSendoProcessada = new Geoinformacao();
		} else {
			lblIdentTituloProduto.setText("<html><b>".concat(geoSendoProcessada.getIdentTituloProduto()).concat("</b></html>"));
			abrirImagem(fileIn.getAbsolutePath());
			habilitarObjetos(true);
		}

		txtIdentDataCriacao.setText(MyUtils.emptyStringIfNull(geoSendoProcessada.getIdentDataCriacao()));
		txtIdentResumo.setText(MyUtils.emptyStringIfNull(geoSendoProcessada.getIdentResumo()));
		opcoesSisrefDatum(MyUtils.emptyStringIfNull(geoSendoProcessada.getSisrefDatum()));
		opcoesSisrefProjecao(MyUtils.emptyStringIfNull(geoSendoProcessada.getSisrefProjecao()));
		txtSisrefObservacao.setText(MyUtils.emptyStringIfNull(geoSendoProcessada.getSisrefObservacao()));
		opcoesIdentcdgTipoReprEspacial(MyUtils.emptyStringIfNull(geoSendoProcessada.getIdentcdgTipoReprEspacial()));
		txtIdentcdgEscala.setText(MyUtils.emptyStringIfNull(geoSendoProcessada.getIdentcdgEscala()));
		opcoesIdentcdgCategoria(MyUtils.emptyStringIfNull(geoSendoProcessada.getIdentcdgCategoria()));
		opcoesIdentcdgMunicipio(MyUtils.emptyStringIfNull(geoSendoProcessada.getIdentcdgMunicipio()));
		txtQualidadeLinhagem.setText(MyUtils.emptyStringIfNull(geoSendoProcessada.getQualidadeLinhagem()));
		opcoesInfadicCamadaInf(MyUtils.emptyStringIfNull(geoSendoProcessada.getInfadicCamadaInf()));
		chkGeorreferenciavel.setSelected(false);
	}

	private boolean validarDados() {
		try {
			MyUtils.obterData(txtIdentDataCriacao.getText(), "dd/MM/yyyy");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Não foi possível validar a Data de Criação do CDG: " + txtIdentDataCriacao.getText());
			return false;
		}
		return true;
	}
	
	private void atualizarStatusArquivo(File arquivo, String status) throws Exception {
		String identTituloProduto = FilenameUtils.removeExtension(arquivo.getName());
		Validacao validacao = MyUtils.entidade(cadastroServico.obterValidacao(null, null, identTituloProduto, null));
		if (validacao == null) {
			validacao = new Validacao();
			validacao.setIdentTituloProduto(identTituloProduto);
			validacao.setNomeArquivo(arquivo.getAbsolutePath());
			validacao.setStatus(status);
			if (status.equalsIgnoreCase("Validar")) {
				validacao.setIdentDataCriacao(validacao.atribuirValor(geoSendoProcessada.getIdentDataCriacao(), txtIdentDataCriacao.getText()));
				validacao.setIdentResumo(validacao.atribuirValor(geoSendoProcessada.getIdentResumo(), txtIdentResumo.getText()));
				validacao.setSisrefDatum(validacao.atribuirValor(geoSendoProcessada.getSisrefDatum(), cbbSisrefDatum.getSelectedItem().toString()));
				validacao.setSisrefProjecao(validacao.atribuirValor(geoSendoProcessada.getSisrefProjecao(), cbbSisrefProjecao.getSelectedItem().toString()));
				validacao.setSisrefObservacao(validacao.atribuirValor(geoSendoProcessada.getSisrefObservacao(), txtSisrefObservacao.getText()));
				validacao.setIdentcdgTipoReprEspacial(validacao.atribuirValor(geoSendoProcessada.getIdentcdgTipoReprEspacial(), cbbIdentcdgTipoReprEspacial.getSelectedItem().toString()));
				validacao.setIdentcdgEscala(validacao.atribuirValor(geoSendoProcessada.getIdentcdgEscala(), txtIdentcdgEscala.getText()));
				validacao.setIdentcdgCategoria(validacao.atribuirValor(geoSendoProcessada.getIdentcdgCategoria(), cbbIdentcdgCategoria.getSelectedItem().toString()));
				validacao.setIdentcdgMunicipio(validacao.atribuirValor(geoSendoProcessada.getIdentcdgMunicipio(), cbbIdentcdgMunicipio.getSelectedItem().toString()));
				validacao.setQualidadeLinhagem(validacao.atribuirValor(geoSendoProcessada.getQualidadeLinhagem(), txtQualidadeLinhagem.getText()));
				validacao.setInfadicCamadaInf(validacao.atribuirValor(geoSendoProcessada.getInfadicCamadaInf(), cbbInfadicCamadaInf.getSelectedItem().toString()));
				validacao.setGeorreferenciavel(chkGeorreferenciavel.isSelected());
			}
		} else {
			if (!validacao.getNomeArquivo().equalsIgnoreCase(arquivo.getAbsolutePath())) {
				JOptionPane.showMessageDialog(null, "ATENÇÃO: o produto '" + identTituloProduto + "' já está na base de dados e inconsistente:\n\n- Arquivo avaliado: " + 
						arquivo.getAbsolutePath() + "\n- Arquivo cadastrado: " + validacao.getNomeArquivo() + "\n\nA análise não foi gravada.");
				return;
			}
		}

		cadastroServico.gravarEntidade(validacao);
	}
	
//	private void abrirImagem(String arquivo) throws Exception {
//	    String [] commands = {
//	        "cmd.exe" , "/c", "start" , "\"DummyTitle\"", "\"" + arquivo + "\""
//	    };
//	    proc = Runtime.getRuntime().exec(commands);
//	}
	
	private void abrirImagem(String arquivo) throws Exception {
		File f = new File(arquivo);
		dt.open(f);
	}
	
//	private void abrirImagem_(String arquivo) throws Exception {
//		String cmd = lblNomeAplicativo.getText() + " \"" + arquivo + "\"";
//
//		Runtime run = Runtime.getRuntime();
//		proc = run.exec(cmd);
//	}
}
