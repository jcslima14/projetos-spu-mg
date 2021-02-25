package views.robo;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.persistence.EntityManager;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

import framework.enums.NivelMensagem;
import framework.utils.MyUtils;
import framework.utils.SpringUtilities;

@SuppressWarnings("serial")
public class Geocodificacao extends JInternalFrame {

	private JTextField txtEndereco = new JTextField(30);
	private JLabel lblEndereco = new JLabel("Endereço:") {{ setLabelFor(txtEndereco); }};
	private JTextField txtChaveAPI = new JTextField(15);
	private JLabel lblChaveAPI = new JLabel("Chave API:") {{ setLabelFor(txtChaveAPI); }};
	private JPanel painelDados = new JPanel() {{ setLayout(new SpringLayout()); }};
	private JButton btnProcessar = new JButton("Processar"); 
	private JTextPane logArea = MyUtils.obterPainelNotificacoes();
	private JScrollPane areaDeRolagem = new JScrollPane(logArea) {{ getViewport().setPreferredSize(new Dimension(1200, 500)); }};

	public Geocodificacao(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		painelDados.add(lblEndereco);
		painelDados.add(txtEndereco);
		painelDados.add(lblChaveAPI);
		painelDados.add(txtChaveAPI);
		painelDados.add(btnProcessar); 
		painelDados.add(new JPanel()); 

		SpringUtilities.makeGrid(painelDados,
	            3, 2, //rows, cols
	            6, 6, //initX, initY
	            6, 6); //xPad, yPad

		add(painelDados, BorderLayout.WEST);
		add(areaDeRolagem, BorderLayout.SOUTH);
		
		btnProcessar.addActionListener(MyUtils.executarProcessoComLog(logArea, new Runnable() {
			@Override
			public void run() {
				geocodificar(txtEndereco.getText(), txtChaveAPI.getText());
			}
		}));
	}

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void geocodificar(String endereco, String chaveAPI) throws RuntimeException {
		try {
			// AIzaSyA_7ozIUbc8SSrGdzm6ovDhfUTOACuUpe4
			GeoApiContext context = new GeoApiContext.Builder().apiKey("AIzaSyA_7ozIUbc8SSrGdzm6ovDhfUTOACuUpe4").build();
			GeocodingResult[] results =  GeocodingApi.geocode(context, endereco).await();
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			MyUtils.appendLogArea(logArea, gson.toJson(results[0]), NivelMensagem.EXCECAO);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
