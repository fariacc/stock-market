package implementation;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import classes.Acao;
import classes.Interesse;
import interfaceRmi.Cliente;
import interfaceRmi.Server;

public class ServidorImplementation extends UnicastRemoteObject implements Server {
	private static final long serialVersionUID = 1L;

	private List<Acao> acoes;
	private List<Interesse> interesses;

	/**
	 * Construtor do servidor
	 */
	public ServidorImplementation() throws RemoteException {
		super();

		this.acoes = new ArrayList<>();
		this.interesses = new ArrayList<>();
	}
	
	/**
	 * Consulta todos os acoes cadastradas
	 */
	@Override
	public List<Acao> consultarAcoes() throws RemoteException {
		return this.acoes;
	}
	
	/**
	 * Consulta uma acao especifica
	 */
	@Override
	public List<Acao> consultarAcaoEspecifica(String codigo) throws RemoteException {
		return this.acoes.stream().filter(acao -> acao.getCodigo().equals(codigo)).map(a -> {
			Acao acao = this.copiar(a);
			acao.setCodigo(codigo);
			acao.setQuantidade(acao.getQuantidade());
			acao.setPreco(acao.getPreco());
			return acao;
		}).collect(Collectors.toList());
	}

	/**
	 * Cadastra uma acao
	 */
	@Override
	public String cadastrarAcao(Acao acao) throws RemoteException {
		Acao novaAcao = this.copiar(acao);

		this.acoes.add(novaAcao);
		return "Acao cadastrada com sucesso";
	}

	/**
	 * Remove uma acao - na realidade sera o metodo de Vender acao
	 */
	@Override
	public String removerAcao(Acao acaoArg) throws RemoteException {
		// Busca o acao a ser removido pelo seu codigo
		Acao acaoRemover = this.acoes.stream().filter(acao -> acao.getCodigo().equals(acaoArg.getCodigo())).findFirst()
				.orElse(null);

		if (acaoRemover == null) {
			return "Acao nao encontrada";
		}

		this.acoes.remove(acaoRemover);
		
		this.notificarLimiteAcao(acaoRemover);

		return "Acao removida com sucesso";
	}

	/**
	 * Compra uma acao
	 */
	@Override
	public synchronized String comprarAcao(Acao acao) throws RemoteException {
		Acao acaoCompra = this.pesquisaAcao(acao.getCodigo());

		if (acaoCompra == null) {
			return "Acao nao encontrada";
		}

		if (!quantidadeSuficienteAcao(acaoCompra, acao.getQuantidade())) {
			return "Quantidade de acao desejada nao disponivel";
		}

		this.descontaQuantidadeAcoesDisponiveis(acaoCompra, acao.getQuantidade());
		this.notificarLimiteAcao(acaoCompra);

		return "Acao comprada com sucesso";
	}

	/**
	 * Consulta os interesses de um dado cliente
	 */
	@Override
	public List<Interesse> consultarInteresses(Cliente referencia) throws RemoteException {
		// Busca os interesses de um cliente pela sua referencia
		return this.interesses.stream().filter(interesse -> interesse.getCliente().equals(referencia))
				.collect(Collectors.toList());
	}

	/**
	 * Registra interesse em um evento
	 */
	@Override
	public String registrarInteresse(Interesse interesse) throws RemoteException {
		Interesse novoInteresse = this.copiar(interesse);

		this.interesses.add(novoInteresse);
		return "Interesse registrado com sucesso";
	}

	/**
	 * Remove o interesse de um evento
	 */
	@Override
	public String removerInteresse(Interesse interesseArg) throws RemoteException {
		// Busca o interesse a ser removido pelo seu ID
		Interesse interesseCancelar = this.interesses.stream()
				.filter(interesse -> interesse.getCodigo().equals(interesseArg.getCodigo())).findFirst().orElse(null);

		if (interesseCancelar == null) {
			return "Interesse nao encontrado";
		}

		this.interesses.remove(interesseCancelar);

		return "Interesse cancelado com sucesso";
	}

	/*
	 * METODOS AUXILIARES
	 */

	/**
	 * Pesquisa uma acao pelo seu codigo
	 */
	private Acao pesquisaAcao(String codigo) {
		return this.acoes.stream().filter(acao -> acao.getCodigo().equals(codigo)).findFirst().orElse(null);
	}

	/**
	 * Valida se um acao tem quantidade suficiente disponivel
	 */
	private Boolean quantidadeSuficienteAcao(Acao acao, Long quantidade) {
		return acao.getQuantidade().compareTo(quantidade) >= 0;
	}

	/**
	 * Desconta a quantidade de uma acao
	 */
	private void descontaQuantidadeAcoesDisponiveis(Acao acao, Long quantidade) {
		acao.setQuantidade(acao.getQuantidade() - quantidade);
		if (acao.getQuantidade().equals(0L)) {
			this.acoes.remove(acao);
		}
	}

	/**
	 * Notifica quando uma acao atingir limite de ganho ou perda desejado - ta com bug
	 */
	private void notificarLimiteAcao(Acao acaoLimite) {
		// Para cada interesse cadastrado
		this.interesses.forEach(interesse -> {

			if (
				(!interesse.getCodigo().equals(acaoLimite.getCodigo()) && interesse.getPrecoMinimo() < acaoLimite.getPreco()) || 
				(!interesse.getCodigo().equals(acaoLimite.getCodigo()) && interesse.getPrecoMaximo() > acaoLimite.getPreco()) &&
				interesse.getQuantidadeDesejada().compareTo(acaoLimite.getQuantidade()) > 0
			) {
				return;
			}

			try {
				// Consulta todas as acoes pra ver se o preco é menor ou maior do que o setado pelo interesse do cliente
				this.consultarAcaoEspecifica(interesse.getCodigo())
						.stream()
						.filter(
							acao -> (
								(acao.getCodigo().equals(acaoLimite.getCodigo()) && acao.getPreco() <= interesse.getPrecoMinimo()) ||
								(acao.getCodigo().equals(acaoLimite.getCodigo()) && acao.getPreco() >= interesse.getPrecoMaximo()) &&
								acao.getQuantidade() > 0
							)
						)
						.forEach(acaoNotificacao -> {
							try {
								interesse.getCliente().notificar(acaoNotificacao);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						});

			} catch (RemoteException e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Copia uma instancia de acao para uma nova.
	 */
	private Acao copiar(Acao acao) {
		Acao novaInstancia = new Acao();
		novaInstancia.setCodigo(acao.getCodigo());
		novaInstancia.setQuantidade(acao.getQuantidade());
		novaInstancia.setPreco(acao.getPreco());

		return novaInstancia;
	}

	/**
	 * Copia uma instancia de interesse para uma nova.
	 */
	private Interesse copiar(Interesse interesse) {
		Interesse novaInstancia = new Interesse();
		novaInstancia.setCodigo(interesse.getCodigo());
		novaInstancia.setCliente(interesse.getCliente());
		novaInstancia.setEventoDesejado(interesse.getEventoDesejado());
		novaInstancia.setQuantidadeDesejada(interesse.getQuantidadeDesejada());
		novaInstancia.setPrecoMinimo(interesse.getPrecoMinimo());
		novaInstancia.setPrecoMaximo(interesse.getPrecoMaximo());

		return novaInstancia;
	}

}
