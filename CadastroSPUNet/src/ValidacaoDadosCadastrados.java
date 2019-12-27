import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.MicrosoftTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffDirectoryConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffImageWriterLossless;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputField;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.io.FilenameUtils;

import framework.MyUtils;
import framework.SpringUtilities;

@SuppressWarnings("serial")
public class ValidacaoDadosCadastrados extends JInternalFrame {

	private EntityManager conexao;
	private JFileChooser filArquivo = new JFileChooser();
	private JButton btnAbrirArquivo = new JButton("Selecionar arquivo");
	private JLabel lblNomeArquivo = new JLabel("") {{ setVerticalTextPosition(SwingConstants.TOP); setSize(600, 20); }};
	private JLabel lblArquivo = new JLabel("Arquivo:", JLabel.TRAILING) {{ setLabelFor(filArquivo); }};
	private JPanel painelDados = new JPanel() {{ setLayout(new SpringLayout()); }};
	private JButton btnProcessar = new JButton("Processar"); 
	private JTextArea logArea = new JTextArea(30, 100);
	private JScrollPane areaDeRolagem = new JScrollPane(logArea);
	private SPUNetServico cadastroServico;

	public ValidacaoDadosCadastrados(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		this.conexao = conexao;
		cadastroServico = new SPUNetServico(this.conexao);

		filArquivo.setFileFilter(new FileNameExtensionFilter("Arquivos TIFF (tif)", "tif"));

		JPanel painelArquivo = new JPanel() {{ add(lblArquivo); add(btnAbrirArquivo); }};

		String espacoEmDisco = MyUtils.verificacaoDeEspacoEmDisco(20);
		
		if (espacoEmDisco != null) {
			painelDados.add(new JLabel("<html><font color='red'>" + espacoEmDisco + "</font></html>"));
			painelDados.add(new JPanel());
		}
		painelDados.add(painelArquivo);
		painelDados.add(lblNomeArquivo);
		painelDados.add(btnProcessar); 
		painelDados.add(new JPanel()); 

		SpringUtilities.makeCompactGrid(painelDados,
	            espacoEmDisco == null ? 2 : 3, 2, //rows, cols
	            6, 6, //initX, initY
	            6, 6); //xPad, yPad

		add(painelDados, BorderLayout.NORTH);
		add(areaDeRolagem, BorderLayout.CENTER);

		btnProcessar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String mensagemErro = null;
				if (lblNomeArquivo.getText().equals("")) {
					mensagemErro = "Para iniciar o processamento é necessário selecionar um arquivo Excel para processar.";
				}

				if (mensagemErro != null) {
					JOptionPane.showMessageDialog(null, mensagemErro);
					return;
				}

				try {
					logArea.setText("");
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								mostrarAtributosArquivo(lblNomeArquivo.getText());;
							} catch (Exception e) {
								MyUtils.appendLogArea(logArea, "Erro ao importar a planilha de dados: \n \n" + e.getMessage() + "\n" + stackTraceToString(e));
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
				// Action detalhes = filArquivo.getActionMap().get("viewTypeDetails");
				// detalhes.actionPerformed(null);
				int retorno = filArquivo.showOpenDialog(ValidacaoDadosCadastrados.this);
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

	private void mostrarAtributosArquivo(String fileIn) throws Exception {
	    TiffImageMetadata exif;
	    ImageMetadata meta = Imaging.getMetadata(new File(fileIn));
        exif = (TiffImageMetadata)meta;
	    TiffOutputSet outputSet = exif.getOutputSet();
	    for (TiffOutputDirectory tod : outputSet.getDirectories()) {
	    	MyUtils.appendLogArea(logArea, "Diretório: " + tod.description());
	    	for (TiffOutputField tof : tod.getFields()) {
	    		String valor = exif.findField(tof.tagInfo).getValue().toString();
	    		MyUtils.appendLogArea(logArea, "Campo: " + tof.tagInfo.name + " (" + tof.tagInfo.length + ") - " + valor);
	    	}
	    }
	    
	    // changeExifMetadata(fileIn);
	    rewriteXpKeywords(fileIn, FilenameUtils.removeExtension(fileIn) + "_.tif");
	}

	private void rewriteXpKeywords(String fileIn, String fileOut) throws Exception {
	    TiffImageMetadata exif;
	    ImageMetadata meta = Imaging.getMetadata(new File(fileIn));
        exif = (TiffImageMetadata)meta;
	    TiffOutputSet outputSet = exif.getOutputSet();
	    TiffOutputDirectory exifDir = outputSet.findDirectory(TiffDirectoryConstants.DIRECTORY_TYPE_ROOT);
	    exifDir.removeField(MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS);
	    exifDir.add(MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS, "validar");

	    BufferedImage img = Imaging.getBufferedImage(new File(fileIn));
	    byte[] imageBytes = Imaging.writeImageToBytes(img, ImageFormats.TIFF, new HashMap<>());
	    
	    File ex = new File(fileOut);
	    FileOutputStream fos = new FileOutputStream(ex);
	    OutputStream os = new BufferedOutputStream(fos);
	    try {
    	    new TiffImageWriterLossless(imageBytes).write(os, outputSet);
	    } finally {
	        if (fos != null) {
	            fos.close();
	        }
	    }
	}

    private void changeExifMetadata(String jpegImageFile) throws Exception {
    	final File src = new File(jpegImageFile);
    	final File dst = new File(FilenameUtils.removeExtension(jpegImageFile) + "_.tif");
        try (FileOutputStream fos = new FileOutputStream(dst);
                OutputStream os = new BufferedOutputStream(fos);) {

            TiffOutputSet outputSet = null;

            // note that metadata might be null if no metadata is found.
            final ImageMetadata metadata = Imaging.getMetadata(src);
            final TiffImageMetadata exif = (TiffImageMetadata) metadata;
            outputSet = exif.getOutputSet();

            {
                final TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
                exifDirectory.removeField(MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS);
                exifDirectory.add(MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS, "validar");
            }

            // printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_DATE_TIME);

            new ExifRewriter().updateExifMetadataLossless(src, os, outputSet);
        }
    }

}
