package views.robos;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import framework.MyException;
import framework.services.SPUNetService;
import framework.utils.MyUtils;
import framework.utils.SpringUtilities;
import models.Geoinformacao;
import services.SPUNetServico;

@SuppressWarnings("serial")
public class BuscaIDCartografia extends JInternalFrame {

	private JTextField txtIdInicial = new JTextField();
	private JLabel lblIdInicial = new JLabel("ID Inicial:") {{ setLabelFor(txtIdInicial); }};
	private JTextField txtIdFinal = new JTextField();
	private JLabel lblIdFinal = new JLabel("ID Final:") {{ setLabelFor(txtIdFinal); }};
	private JComboBox<String> cbbNavegador = new JComboBox<String>();
	private JLabel lblNavegador = new JLabel("Navegador:") {{ setLabelFor(cbbNavegador); }};
	private SPUNetServico cadastroServico;

	public BuscaIDCartografia(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		cadastroServico = new SPUNetServico(conexao);

		// define os objetos da tela
		JLabel lblUsuario = new JLabel("Usuário:");
		JTextField txtUsuario = new JTextField(15);
		lblUsuario.setLabelFor(txtUsuario);

		JLabel lblSenha = new JLabel("Senha:");
		JPasswordField txtSenha = new JPasswordField(15);
		lblSenha.setLabelFor(txtSenha);

		JPanel painelDados = new JPanel();
		painelDados.setLayout(new SpringLayout());
		JButton botaoProcessar = new JButton("Processar"); 

		cbbNavegador.addItem("Chrome");
		cbbNavegador.addItem("Firefox");

		String espacoEmDisco = MyUtils.verificacaoDeEspacoEmDisco(20);
		
		if (espacoEmDisco != null) {
			painelDados.add(new JLabel("<html><font color='red'>" + espacoEmDisco + "</font></html>"));
			painelDados.add(new JPanel());
		}
		painelDados.add(lblUsuario);
		painelDados.add(txtUsuario);
		painelDados.add(lblSenha);
		painelDados.add(txtSenha);
		painelDados.add(lblIdInicial);
		painelDados.add(txtIdInicial);
		painelDados.add(lblIdFinal);
		painelDados.add(txtIdFinal);
		painelDados.add(lblNavegador);
		painelDados.add(cbbNavegador);
		painelDados.add(botaoProcessar); 
		painelDados.add(new JPanel());

		SpringUtilities.makeGrid(painelDados,
                espacoEmDisco == null ? 6 : 7, 2, //rows, cols
                6, 6, //initX, initY
                6, 6); //xPad, yPad

		add(painelDados, BorderLayout.WEST);
		JTextArea logArea = new JTextArea(30, 100);
		JScrollPane areaDeRolagem = new JScrollPane(logArea);
		add(areaDeRolagem, BorderLayout.SOUTH);

		botaoProcessar.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				logArea.setText("");
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							buscarIDSPUNet(logArea, txtUsuario.getText(), new String(txtSenha.getPassword()));
						} catch (Exception e) {
							MyUtils.appendLogArea(logArea, "Erro ao processar a carga: \n \n" + e.getMessage() + "\n" + MyUtils.stackTraceToString(e));
							e.printStackTrace();
						}
					}
				}).start();
			} 
		}); 
    }

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void buscarIDSPUNet(JTextArea logArea, String usuario, String senha) throws Exception {
		MyUtils.appendLogArea(logArea, "Iniciando o navegador web...");
		SPUNetService spunetService = new SPUNetService(cbbNavegador.getSelectedItem().toString(), "http://spunet.planejamento.gov.br", true);
		spunetService.login(usuario, senha);

        int idInicial = Integer.parseInt(txtIdInicial.getText());
        int idFinal = Integer.parseInt(txtIdFinal.getText());

        // inicia o loop para leitura dos arquivos do diretório
        for (int idPesquisar = idInicial; idPesquisar <= idFinal; idPesquisar++) {
            spunetService.esperarCarregamento(500, 5, 1, "//p[contains(text(), 'Carregando')]"); 

	        MyUtils.appendLogArea(logArea, MyUtils.formatarData(new Date(),  "dd/MM/yyyy HH:mm:ss") + " - Processando id " + idPesquisar + " de " + idFinal);

            // clica no menu da aplicação
	        spunetService.navegarPaginaMetadadoPorId(idPesquisar);
	        String tituloProdutoCartografico = null;
	        try {
	        	tituloProdutoCartografico = spunetService.retornarTituloProdutoCartografico();
	        } catch (MyException e) {
	        	MyUtils.appendLogArea(logArea, e.getMessage());
	        	continue;
	        }

	        Geoinformacao geo = MyUtils.entidade(cadastroServico.obterGeoinformacao(null, null, tituloProdutoCartografico));
	        
	        if (geo == null) {
	        	MyUtils.appendLogArea(logArea, "Não foi encontrado na base de dados o registro para o produto cartográfico '" + tituloProdutoCartografico + "'");
	        	continue;
	        }

	        if (geo.getIdSPUNet() != null) {
	        	MyUtils.appendLogArea(logArea, "O produto cartográfico '" + tituloProdutoCartografico + "' já está com o ID do SPUNet atualizado na base de dados");
	        	continue;
	        }
	        
	        geo.setIdSPUNet(idPesquisar);
	        cadastroServico.gravarEntidade(geo);
	        
	        MyUtils.appendLogArea(logArea, "O produto cartográfico '" + tituloProdutoCartografico + "' foi atualizado com o ID " + idPesquisar);
        }

		MyUtils.appendLogArea(logArea, "Fim do processamento...");
        spunetService.fechaNavegador();;
	}
}
