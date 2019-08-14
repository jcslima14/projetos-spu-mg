import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

import framework.MyUtils;
import framework.SpringUtilities;

@SuppressWarnings("serial")
public class ExecucaoScript extends JInternalFrame {

	private Connection conexao;

	public ExecucaoScript(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		this.conexao = conexao;

		JPanel painelDados = new JPanel();
		painelDados.setLayout(new SpringLayout());
		JButton botaoProcessar = new JButton("Processar"); 

		String espacoEmDisco = MyUtils.verificacaoDeEspacoEmDisco(20);

		if (espacoEmDisco != null) {
			painelDados.add(new JLabel("<html><font color='red'>" + espacoEmDisco + "</font></html>"));
		}

		painelDados.add(botaoProcessar); 

		SpringUtilities.makeGrid(painelDados,
                espacoEmDisco == null ? 1 : 2, 1, //rows, cols
                6, 6, //initX, initY
                6, 6); //xPad, yPad

		add(painelDados, BorderLayout.NORTH);
		JTextArea logArea = new JTextArea(30, 100);
		JScrollPane areaDeRolagem = new JScrollPane(logArea);
		add(areaDeRolagem, BorderLayout.CENTER);

		botaoProcessar.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							executarScript(logArea);
							JOptionPane.showMessageDialog(null, "Script executado com sucesso!");
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "Erro ao executar o script: \n \n" + e.getMessage() + "\n" + stackTraceToString(e));
							e.printStackTrace();
						}
					}

					private String stackTraceToString(Exception e) {
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						return sw.toString();
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

	private void executarScript(JTextArea logArea) throws Exception {
		for (String sql : logArea.getText().split(";")) {
			MyUtils.execute(conexao, sql);
		}
	}
}
