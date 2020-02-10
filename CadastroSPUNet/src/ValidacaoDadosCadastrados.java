import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
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
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.MicrosoftTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffDirectoryConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffImageWriterLossless;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
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
	private MyButton btnProximo = new MyButton("Próximo");
	private MyButton btnValidar = new MyButton("Validar");
	private MyButton btnRevisar = new MyButton("Revisar");

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

		filAplicativo.setFileFilter(new FileNameExtensionFilter("Aplicações (*.exe)", "exe"));

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
				processarArquivo();
			}
		});

		btnRevisar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processarArquivo();
			}
		});

		btnProcessar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String mensagemErro = "";
				if (lblNomePasta.getText().equals("")) {
					mensagemErro += "Para iniciar o processamento é necessário selecionar uma pasta para ser processada. \n";
				}

				if (lblNomeAplicativo.getText().equals("")) {
					mensagemErro += "Para iniciar o processamento é necessário selecionar um aplicativo de leitura de imagens. \n";
				}

				if (!mensagemErro.equals("")) {
					JOptionPane.showMessageDialog(null, mensagemErro);
					return;
				}

				indiceArquivo = 0;
				arquivosAProcessar = new ArrayList<File>();

				logArea.setText("Aguarde a obtenção da lista de arquivos...");
				
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
						MyUtils.salvarConfiguracaoLocal("aplicativoimagem", lblNomeAplicativo.getText(), "Não foi possível salvar o aplicativo de imagem");
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
				arquivosAProcessar.add(arquivo);
			}
		}
	}

	private void habilitarBotoes() {
		btnProximo.setEnabled(arquivosAProcessar != null && arquivosAProcessar.size() > indiceArquivo);
		btnValidar.setEnabled(arquivosAProcessar != null && arquivosAProcessar.size() > 0);
		btnRevisar.setEnabled(arquivosAProcessar != null && arquivosAProcessar.size() > 0);
	}

	private void processarArquivo() {
		File arquivoAProcessar = arquivosAProcessar.get(indiceArquivo++);
		habilitarBotoes();
		try {
			processarArquivo(arquivoAProcessar);
		} catch (Exception e) {
			logArea.setText("Erro ao processar o arquivo '" + arquivoAProcessar.getAbsolutePath() + "': \n\n" + e.getMessage());
		}
	}

	private void processarArquivo(File fileIn) throws Exception {
		String html = "<h1><b><u>".concat("Arquivo:</u></b>").concat(" ").concat(fileIn.getAbsolutePath()).concat("</h1>");

		Geoinformacao geo = MyUtils.entidade(cadastroServico.obterGeoinformacao(null, true, FilenameUtils.removeExtension(fileIn.getName())));

		if (geo == null) {
			html += "<p><font color='red'>".concat("Registro não encontrado na base de dados!").concat("</font></p>");
		} else {
			html += "<p><tt><b>Título do Produto:</b>".concat(" ").concat(geo.getIdentTituloProduto()).concat("</tt></p>");
			html += "<p><tt><b>Data de Criação..:</b>".concat(" ").concat(geo.getIdentDataCriacao()).concat("</tt></p>");
			html += "<p><tt><b>Resumo...........:</b>".concat(" ").concat(geo.getIdentResumo()).concat("</tt></p>");
		}
		
		logArea.setText(html);

	    // gravarXpKeywords(fileIn, new File(FilenameUtils.removeExtension(fileIn.getAbsolutePath()) + "_tmp.tif"));
	}

	private void gravarXpKeywords(File fileIn, File fileOut) throws Exception {
	    TiffImageMetadata exif;
	    ImageMetadata meta = Imaging.getMetadata(fileIn);
        exif = (TiffImageMetadata) meta;
	    TiffOutputSet outputSet = exif.getOutputSet();
	    TiffOutputDirectory exifDir = outputSet.findDirectory(TiffDirectoryConstants.DIRECTORY_TYPE_ROOT);
	    exifDir.removeField(MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS);
	    exifDir.add(MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS, "validar");

	    BufferedImage img = Imaging.getBufferedImage(fileIn);
	    byte[] imageBytes = Imaging.writeImageToBytes(img, ImageFormats.TIFF, new HashMap<>());
	    
	    FileOutputStream fos = new FileOutputStream(fileOut);
	    OutputStream os = new BufferedOutputStream(fos);
	    try {
    	    new TiffImageWriterLossless(imageBytes).write(os, outputSet);
	    } finally {
	        if (fos != null) {
	            fos.close();
	            reatribuirDataCorreta(fileIn, fileOut);
	        }
	    }
	}

	private void reatribuirDataCorreta(File fileIn, File fileOut) throws Exception {
		BasicFileAttributes attr = Files.readAttributes(fileIn.toPath(), BasicFileAttributes.class);

		Files.setAttribute(fileOut.toPath(), "creationTime", attr.creationTime());
		Files.setAttribute(fileOut.toPath(), "lastAccessTime", attr.lastAccessTime());
		Files.setAttribute(fileOut.toPath(), "lastModifiedTime", attr.lastModifiedTime());

		// renomear o arquivo destino e apagar o arquivo origem
		String nomeArquivoOriginal = fileIn.getAbsolutePath();
		MyUtils.renomearArquivo(nomeArquivoOriginal, FilenameUtils.removeExtension(nomeArquivoOriginal) + "_old.tif", 10, false);
		MyUtils.renomearArquivo(fileOut.getAbsolutePath(), nomeArquivoOriginal, 10, false);
		if (MyUtils.arquivoExiste(nomeArquivoOriginal)) {
			(new File(FilenameUtils.removeExtension(nomeArquivoOriginal) + "_old.tif")).delete();
		}
	}

	private void abrirImagem(String arquivo) throws Exception {
		String cmd = lblAplicativo.getText() + " \"" + arquivo + "\"";

		Runtime run = Runtime.getRuntime();
		proc = run.exec(cmd);
	}
//
//	private void mostrarAtributosArquivo(String fileIn) throws Exception {
//	    TiffImageMetadata exif;
//	    ImageMetadata meta = Imaging.getMetadata(new File(fileIn));
//        exif = (TiffImageMetadata)meta;
//	    TiffOutputSet outputSet = exif.getOutputSet();
//	    for (TiffOutputDirectory tod : outputSet.getDirectories()) {
//	    	MyUtils.appendLogArea(logArea, "Diretório: " + tod.description());
//	    	for (TiffOutputField tof : tod.getFields()) {
//	    		String valor = exif.findField(tof.tagInfo).getValue().toString();
//	    		MyUtils.appendLogArea(logArea, "Campo: " + tof.tagInfo.name + " (" + tof.tagInfo.length + ") - " + valor);
//	    	}
//	    }
//	}
}
