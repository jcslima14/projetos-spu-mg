package framework;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public abstract class CadastroController extends JPanel {

	private MyTable tabela = new MyTable();
	private MyButton btnAtualizar = new MyButton("Atualizar") {{ setExclusao(true); }};
	private MyButton btnIncluir = new MyButton("Incluir") {{ setExclusao(true); }};
	private MyButton btnExcluir = new MyButton("Excluir") {{ setExclusao(true); }};
	private MyButton btnSalvar = new MyButton("Salvar") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyButton btnCancelar = new MyButton("Cancelar") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyButton[] btnBotoesPosteriores = null;
	private JPanel pnlCamposEditaveis = null;
	private JPanel pnlFiltros = null;
	private boolean exibirTabelaDados = true;
	private boolean exibirBotoesCadastro = true;
	private boolean exibirBotaoCancelarEdicao = true;
	private boolean exibirBotaoIncluir = true;

	public MyTable getTabela() {
		return this.tabela;
	}

	public void setPnlCamposEditaveis(JPanel pnlCamposEditaveis) {
		this.pnlCamposEditaveis = pnlCamposEditaveis;
	}

	public void setPnlFiltros(JPanel pnlFiltros) {
		this.pnlFiltros = pnlFiltros;
	}

	public void setBtnBotoesPosteriores(MyButton... btnBotoesPosteriores) {
		this.btnBotoesPosteriores = btnBotoesPosteriores;
	}

	public void setExibirTabelaDados(boolean exibirTabelaDados) {
		this.exibirTabelaDados = exibirTabelaDados;
	}

	public void setExibirBotoesCadastro(boolean exibirBotoesCadastro) {
		this.exibirBotoesCadastro = exibirBotoesCadastro;
	}
	
	public void setExibirBotaoCancelarEdicao(boolean exibirBotaoCancelarEdicao) {
		this.exibirBotaoCancelarEdicao = exibirBotaoCancelarEdicao;
	}

	public void setExibirBotaoIncluir(boolean exibirBotaoIncluir) {
		this.exibirBotaoIncluir = exibirBotaoIncluir;
	}

	public void inicializar() {
		this.setLayout(new BorderLayout());
		// define se deve ser exibida a tabela de dados
		if (exibirTabelaDados) {
			JScrollPane areaRolavel = new JScrollPane(tabela);
			areaRolavel.setVisible(true);
			add(areaRolavel, BorderLayout.CENTER);

			try {
				resetarDados();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

		// define se deve ser exibido o painel superior da janela (criado se exibir botões de cadastro, ou botões adicionais, ou painel de filtros)
		if (exibirBotoesCadastro || btnBotoesPosteriores != null || pnlFiltros != null) {
			JPanel pnlPainelSuperior = new JPanel();
			pnlPainelSuperior.setLayout(new BoxLayout(pnlPainelSuperior, BoxLayout.Y_AXIS));

			// criado o painel de botões se for para exibir os botões de cadastro ou botões adicionais
			if (exibirBotoesCadastro || btnBotoesPosteriores != null) {
				JPanel pnlBotoesAcima = new JPanel(new FlowLayout());

				if (exibirBotoesCadastro) {
					pnlBotoesAcima.add(btnAtualizar);
					if (exibirBotaoIncluir) pnlBotoesAcima.add(btnIncluir);
					pnlBotoesAcima.add(btnExcluir);
				}
	
				if (btnBotoesPosteriores != null) {
					for (MyButton botao : btnBotoesPosteriores) {
						pnlBotoesAcima.add(botao);
					}
				}

				pnlPainelSuperior.add(pnlBotoesAcima);
			}

			// verifica se o painel de filtros deve ser exibido
			if (pnlFiltros != null) {
				pnlPainelSuperior.add(pnlFiltros);
			}
			
			add(pnlPainelSuperior, BorderLayout.NORTH);
		}

		// verifica se o painel de campos editáveis deve ser exibido
		if (pnlCamposEditaveis != null) {
			JPanel pnlBotoesAbaixo = new JPanel(new FlowLayout());
			pnlBotoesAbaixo.add(btnSalvar);
			if (exibirBotaoCancelarEdicao) {
				pnlBotoesAbaixo.add(btnCancelar);
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
				try {
					incluirRegistro();
					definirPermissoesInclusao();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Erro ao tentar incluir registro: \n\n" + e1.getMessage());
					e1.printStackTrace();
				}
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
				if (exibirTabelaDados) {
					cancelarEdicao();
					limparCamposEditaveis();
				}
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
					definirPermissoesEdicao();
		        }
			}
		});
	}

	public abstract void incluirRegistro() throws Exception;
	
	public abstract void excluirRegistro(Integer id) throws Exception;

	public abstract void salvarRegistro() throws Exception;

	public abstract void prepararParaEdicao();

	public abstract void limparCamposEditaveis();

	public abstract TableModel obterDados() throws Exception;
	
	public abstract List<MyTableColumn> getColunas();

	public void doEditarRegistro() {
		prepararParaEdicao();
		definirPermissoesEdicao();
	}
	
	private void definirPermissoesEdicao() {
		componentesEmEdicao(this, true);
	}

	private void definirPermissoesInclusao() {
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

	private void resetarDados() throws Exception {
		if (exibirTabelaDados) {
			tabela.setModel(obterDados());
			tabela.resizeColumns(getColunas(), true);
		}
	}

	public void executarAtualizar() {
		btnAtualizar.doClick();
	}
}
