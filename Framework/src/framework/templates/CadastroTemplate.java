package framework.templates;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import framework.components.MyButton;
import framework.components.MyTable;
import framework.components.MyTableColumn;
import framework.models.PropriedadesEdicao;
import framework.utils.MyUtils;

@SuppressWarnings("serial")
public abstract class CadastroTemplate extends JInternalFrame {

	private MyTable tabela = new MyTable();
	private MyButton btnAtualizar = new MyButton("Atualizar", MyUtils.obterIcone("resources/icons/008-sync.png")) {{ setExclusao(true); }};
	private MyButton btnIncluir = new MyButton("Incluir", MyUtils.obterIcone("resources/icons/056-add.png")) {{ setExclusao(true); }};
	private MyButton btnExcluir = new MyButton("Excluir", MyUtils.obterIcone("resources/icons/006-trash.png")) {{ setExclusao(true); }};
	private MyButton btnSalvar = new MyButton("Salvar", MyUtils.obterIcone("resources/icons/057-check.png")) {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyButton btnCancelar = new MyButton("Cancelar", MyUtils.obterIcone("resources/icons/047-cancel-1.png")) {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyButton[] btnBotoesAcimaPosteriores = null;
	private MyButton[] btnBotoesAbaixoPosteriores = null;
	private JPanel pnlCamposEditaveis;
	private JPanel pnlFiltros = null;
	private boolean exibirBotaoIncluir = true;

	public void setExibirBotaoIncluir(boolean exibirBotaoIncluir) {
		this.exibirBotaoIncluir = exibirBotaoIncluir;
	}

	public MyTable getTabela() {
		return this.tabela;
	}

	public void setPnlCamposEditaveis(JPanel pnlCamposEditaveis) {
		this.pnlCamposEditaveis = pnlCamposEditaveis;
	}

	public void setPnlFiltros(JPanel pnlFiltros) {
		this.pnlFiltros = pnlFiltros;
	}

	public void setBtnBotoesAcimaPosteriores(MyButton... btnBotoesAcimaPosteriores) {
		this.btnBotoesAcimaPosteriores = btnBotoesAcimaPosteriores;
	}

	public void setBtnBotoesAbaixoPosteriores(MyButton... btnBotoesAbaixoPosteriores) {
		this.btnBotoesAbaixoPosteriores = btnBotoesAbaixoPosteriores;
	}

	public CadastroTemplate() {
		super();
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);
		setSize(1000, 500);
	}

	public CadastroTemplate(String tituloJanela) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);
		setSize(1000, 500);
	}

	public void inicializar() {
		inicializar(true);
	}
	
	public void inicializar(boolean mostrarAreaEdicao) {
		JScrollPane areaRolavel = new JScrollPane(tabela);
		areaRolavel.setVisible(true);
		add(areaRolavel, BorderLayout.CENTER);

		try {
			resetarDados();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		
		JPanel pnlPainelSuperior = new JPanel();
		pnlPainelSuperior.setLayout(new BoxLayout(pnlPainelSuperior, BoxLayout.Y_AXIS));
		JPanel pnlBotoesAcima = new JPanel(new FlowLayout());
		pnlBotoesAcima.add(btnAtualizar);
		if (exibirBotaoIncluir) pnlBotoesAcima.add(btnIncluir);
		pnlBotoesAcima.add(btnExcluir);

		if (btnBotoesAcimaPosteriores != null) {
			for (MyButton botao : btnBotoesAcimaPosteriores) {
				pnlBotoesAcima.add(botao);
			}
		}

		pnlPainelSuperior.add(pnlBotoesAcima);
		if (pnlFiltros != null) {
			pnlPainelSuperior.add(pnlFiltros);
		}
		
		add(pnlPainelSuperior, BorderLayout.NORTH);

		if (mostrarAreaEdicao) {
			JPanel pnlBotoesAbaixo = new JPanel(new FlowLayout());
			pnlBotoesAbaixo.add(btnSalvar);
			pnlBotoesAbaixo.add(btnCancelar);

			if (btnBotoesAbaixoPosteriores != null) {
				for (MyButton botao : btnBotoesAbaixoPosteriores) {
					pnlBotoesAbaixo.add(botao);
				}
			}

			JPanel pnlAreaEdicao = new JPanel();
			pnlAreaEdicao.setLayout(new BoxLayout(pnlAreaEdicao, BoxLayout.PAGE_AXIS));
			pnlAreaEdicao.add(this.pnlCamposEditaveis);
			pnlAreaEdicao.add(pnlBotoesAbaixo);
	
			add(pnlAreaEdicao, BorderLayout.SOUTH);
		}

		btnAtualizar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					resetarDados();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Erro ao tentar atualizar a lista de registros:\n\n" + e1.getMessage());
					e1.printStackTrace();
				}
			}
		});

		btnIncluir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				incluirRegistro();
			}
		});

		btnCancelar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelarEdicao();
				limparCamposEditaveis();
			}
		});

		btnSalvar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					salvarRegistro();
					resetarDados();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				cancelarEdicao();
				limparCamposEditaveis();
			}
		});

		btnExcluir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int resposta = JOptionPane.showConfirmDialog(null, "ATENÇÃO! Esta ação não poderá ser desfeita! Confirma a exclusão dos registros selecionados?", "Confirmação de Exclusão", JOptionPane.YES_NO_OPTION);
				if (resposta == JOptionPane.NO_OPTION) {
					return;
				}

				try {
					for (int i = 0; i < tabela.getRowCount(); i++) {
						Boolean selecionado = (Boolean) tabela.getValueAt(i, 0);
						Integer id = (Integer) tabela.getValueAt(i, 1);
						if (selecionado.equals(Boolean.TRUE)) {
							excluirRegistro(id);
						}
					}
					resetarDados();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		tabela.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {
		        JTable table = (JTable) mouseEvent.getSource();
		        if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
		        	prepararParaEdicao();
					editarRegistro();
		        }
			}
		});
	}

	public abstract void excluirRegistro(Integer id) throws Exception;

	public abstract void salvarRegistro() throws Exception;

	public abstract void prepararParaEdicao();

	public abstract void limparCamposEditaveis();

	public abstract TableModel obterDados() throws Exception;
	
	public abstract List<MyTableColumn> getColunas();

	private void editarRegistro() {
		componentesEmEdicao(this, true);
	}

	public void incluirRegistro() {
		componentesEmInclusao(this, true);
	}

	private void componentesEmInclusao(Container container, boolean caracterizar) {
		Component[] componentes = container.getComponents();
		for (Component componente : componentes) {
			if (componente instanceof PropriedadesEdicao) {
				if (((PropriedadesEdicao) componente).isInclusao()) {
					componente.setEnabled(caracterizar);
				}
			} else if (componente instanceof Container) {
				componentesEmInclusao((Container) componente, caracterizar);
			}
		}
	}

	private void componentesEmEdicao(Container container, boolean caracterizar) {
		Component[] componentes = container.getComponents();
		for (Component componente : componentes) {
			if (componente instanceof PropriedadesEdicao) {
				if (((PropriedadesEdicao) componente).isEdicao()) {
					componente.setEnabled(caracterizar);
				}
			} else if (componente instanceof Container) {
				componentesEmEdicao((Container) componente, caracterizar);
			}
		}
	}

	private void cancelarEdicao() {
		componentesEmEdicao(this, false);
		componentesEmInclusao(this, false);
	}

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		try {
			this.setMaximum(true);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		this.setVisible(true);
		this.show();
	}

	private void resetarDados() throws Exception {
		tabela.setModel(obterDados());
		tabela.resizeColumns(getColunas(), true);
	}

	public void executarAtualizar() {
		btnAtualizar.doClick();
	}
}
