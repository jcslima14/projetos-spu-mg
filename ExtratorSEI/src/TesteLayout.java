import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

@SuppressWarnings("serial")
public class TesteLayout extends JInternalFrame {

	public TesteLayout(String tituloJanela, Connection conexao) {
		super("Carga de Informações do SEI");
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);
		
		// define os objetos da tela
		JLabel lblUsuario = new JLabel("Usuário:");
		JTextField txtUsuario = new JTextField(15);
//		txtUsuario.setPreferredSize(new Dimension(400, 25));
//		txtUsuario.setMaximumSize(new Dimension(400, 25));
		lblUsuario.setLabelFor(txtUsuario);
		
		JLabel lblSenha = new JLabel("Senha:");
		JPasswordField txtSenha = new JPasswordField(15);
//		txtSenha.setMaximumSize(new Dimension(400, 25));
//		txtSenha.setPreferredSize(new Dimension(400, 25));
		lblSenha.setLabelFor(txtSenha);
		
		JLabel lblDataInicial = new JLabel("Data Inicial:");
		JTextField txtDataInicial = new JTextField(15);
//		txtDataInicial.setMaximumSize(new Dimension(400, 25));
//		txtDataInicial.setPreferredSize(new Dimension(400, 25));
		lblDataInicial.setLabelFor(txtDataInicial);

		JLabel lblDataFinal = new JLabel("Data Final:");
		JTextField txtDataFinal = new JTextField(15);
//		txtDataFinal.setMaximumSize(new Dimension(400, 25));
//		txtDataFinal.setPreferredSize(new Dimension(400, 25));
		lblDataFinal.setLabelFor(txtDataFinal);

		JPanel painelDados = new JPanel();
		
		painelDados.setLayout(new GridLayout(5, 2));
		painelDados.setMaximumSize(new Dimension(400, 130));
		painelDados.setPreferredSize(new Dimension(400, 130));
		painelDados.setAlignmentX(LEFT_ALIGNMENT);
		painelDados.setBorder(new LineBorder(new Color(0, 0, 0)));
		painelDados.setBounds(0, 0, 400, 130);
		JButton botaoCarregar = new JButton("Carregar"); 
		JButton botaoSair = new JButton("Sair"); 

		painelDados.add(lblUsuario);
		painelDados.add(txtUsuario);
		painelDados.add(lblSenha);
		painelDados.add(txtSenha);
		painelDados.add(lblDataInicial);
		painelDados.add(txtDataInicial);
		painelDados.add(lblDataFinal);
		painelDados.add(txtDataFinal);
		painelDados.add(botaoCarregar); 
		painelDados.add(botaoSair);

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		
		add(painelDados, BorderLayout.NORTH);
		JTextArea logArea = new JTextArea(30, 100);
		JScrollPane areaDeRolagem = new JScrollPane(logArea);
		add(areaDeRolagem, BorderLayout.CENTER);

		botaoCarregar.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				String msg = "W: " + painelDados.getWidth() + " - H: " + painelDados.getHeight() + " - X: "+ painelDados.getX() + " - Y: "+ painelDados.getY();
				JOptionPane.showMessageDialog(null, msg);
				logArea.setText("");
			} 
		}); 

		botaoSair.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				System.exit(0);
			} 
		}); 
    }

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack();
		this.setVisible(true);
		this.show();
	}
}
