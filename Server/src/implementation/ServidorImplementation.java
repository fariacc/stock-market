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
	
	private List<Acao> acoes; //lista de acoes que o cliente possui
	private List<Interesse> interesses; //lista de acoes que o cliente deseja ser notificado quando atingirem limites de ganho/perda
	private List<Acao> cotacoes; //lista de acoes que o cliente precisa/deseja monitorar (pode ou não ter essas acoes em carteira)

	//Construtor do servidor
	public ServidorImplementation() throws RemoteException {
		super();

		this.acoes = new ArrayList<>();
		this.interesses = new ArrayList<>();
		this.cotacoes = new ArrayList<>();
	}
	
	//Consulta todas os acoes cadastradas na carteira - FUNCIONANDO
	@Override
	public List<Acao> consultarCarteira() throws RemoteException {
		return this.acoes;
	}
	
	//Consulta uma acao especifica na carteira - FUNCIONANDO
	@Override
	public List<Acao> consultarCarteiraAcaoEspecifica(String codigoArg) throws RemoteException {
		return this.acoes.stream().filter(acao -> acao.getCodigo().equals(codigoArg)).map(a -> {
			Acao acao = this.copiar(a);
			acao.setCodigo(codigoArg);
			acao.setQuantidade(acao.getQuantidade());
			acao.setPreco(acao.getPreco());
			return acao;
		}).collect(Collectors.toList());
	}

	//Cadastra uma acao na carteira - FUNCIONANDO
	@Override
	public String cadastrarAcaoCarteira(Acao acaoArg) throws RemoteException {
		Acao novaAcao = this.copiar(acaoArg);

		this.acoes.add(novaAcao);
		return "Acao cadastrada com sucesso";
	}

	//Remove uma acao da carteira
	@Override
	public String removerAcaoCarteira(Acao acaoArg) throws RemoteException {
		// Busca o acao a ser removido pelo seu codigo
		Acao acaoRemover = this.acoes.stream().filter(acao -> acao.getCodigo().equals(acaoArg.getCodigo())).findFirst().orElse(null);

		if (acaoRemover == null) {
			return "Acao nao encontrada";
		}

		this.acoes.remove(acaoRemover);

		return "Acao removida com sucesso";
	}
	
	//Compra uma acao e notifica o cliente
	@Override
	public synchronized String comprarAcao(Cliente clienteArg, Acao acaoArg) throws RemoteException {
		Acao acaoComprar = this.acoes.stream().filter(acao -> acao.getCodigo().equals(acaoArg.getCodigo())).findFirst().orElse(null);

		if (acaoComprar == null) {
			return "Acao nao encontrada";
		}

		if (!quantidadeSuficienteAcao(acaoComprar, acaoArg.getQuantidade())) {
			return "Quantidade de acao desejada nao disponivel";
		}
		
		if (acaoArg.getPreco() < acaoComprar.getPreco()) {
			return "O preco unitario eh maior do que seu preco maximo a pagar";
		}

		this.descontaQuantidadeAcoesDisponiveis(acaoComprar, acaoArg.getQuantidade());
		
		clienteArg.notificar("A acao foi comprada com sucesso", acaoArg);

		return "";
	}
	
	//Vende uma acao e notifica o cliente
	@Override
	public synchronized String venderAcao(Cliente clienteArg, Acao acaoArg) throws RemoteException {
		Acao acaoVender = this.cotacoes.stream().filter(acao -> acao.getCodigo().equals(acaoArg.getCodigo())).findFirst().orElse(null);
		
		if (!quantidadeSuficienteAcao(acaoArg, acaoArg.getQuantidade())) {
			return "Voce nao possui essa quantidade para vender";
		}
		
		acaoVender.setQuantidade(acaoArg.getQuantidade() + acaoVender.getQuantidade());
		acaoVender.setPreco(acaoArg.getPreco());
		
		clienteArg.notificar("A acao foi vendida com sucesso", acaoArg);

		return "";
	}

	//Consulta os interesses de um determinado cliente
	@Override
	public List<Interesse> consultarInteresses(Cliente clienteArg) throws RemoteException {
		// Busca os interesses de um cliente pela sua referencia
		return this.interesses.stream().filter(interesse -> interesse.getCliente().equals(clienteArg)).collect(Collectors.toList());
	}

	//Registra interesse no evento
	@Override
	public String registrarInteresse(Interesse interesseArg) throws RemoteException {
		Interesse novoInteresse = this.copiar(interesseArg);

		this.interesses.add(novoInteresse);
		return "Interesse registrado com sucesso";
	}

	//Remove o interesse no evento
	@Override
	public String removerInteresse(Interesse interesseArg) throws RemoteException {
		// Busca o interesse a ser removido pelo codigo da acao
		Interesse interesseCancelar = this.interesses.stream()
			.filter(interesse -> interesse.getCodigo().equals(interesseArg.getCodigo())).findFirst().orElse(null);

		if (interesseCancelar == null) {
			return "Interesse nao encontrado";
		}

		this.interesses.remove(interesseCancelar);

		return "Interesse cancelado com sucesso";
	}
	
	//Obtem cotacao de todas as acoes - FUNCIONANDO
	@Override
	public List<Acao> obterCotacoes() throws RemoteException {
		return this.cotacoes;
	}
	
	//Obtem cotacao de uma acao especifica - FUNCIONANDO
	@Override
	public List<Acao> obterCotacaoAcaoEspecifica(String codigoArg) throws RemoteException {
		return this.cotacoes.stream().filter(cotacao -> cotacao.getCodigo().equals(codigoArg)).map(c -> {
			Acao cotacao = this.copiar(c);
			cotacao.setCodigo(codigoArg);
			cotacao.setQuantidade(cotacao.getQuantidade());
			cotacao.setPreco(cotacao.getPreco());
			return cotacao;
		}).collect(Collectors.toList());
	}
	
	//Insere uma acao na lista de cotacoes - FUNCIONANDO
	@Override
	public String cadastrarAcaoCotacoes(Cliente clienteArg, Acao cotacaoArg) throws RemoteException {
		Acao novaCotacao = this.copiar(cotacaoArg);

		this.cotacoes.add(novaCotacao);
		return "Cotacao de acao cadastrada com sucesso";
	}

	//Remove uma acao das cotacoes - FUNCIONANDO
	@Override
	public String removerCotacaoAcaoEspecifica(Acao acaoArg) throws RemoteException {
		// Busca o cotacao da acao a ser removida1 pelo seu codigo
		Acao cotacaoRemover = this.cotacoes.stream().filter(cotacao -> cotacao.getCodigo().equals(acaoArg.getCodigo())).findFirst().orElse(null);

		if (cotacaoRemover == null) {
			return "Cotacao de acao nao encontrada";
		}

		this.cotacoes.remove(cotacaoRemover);

		return "Cotacao de acao removida com sucesso";
	}	
	
	//---------------------------------------------------------

	//Valida se um acao tem quantidade suficiente disponivel
	private Boolean quantidadeSuficienteAcao(Acao acaoArg, Long quantidadeArg) {
		return acaoArg.getQuantidade().compareTo(quantidadeArg) >= 0;
	}

	//Desconta a quantidade de uma acao
	private void descontaQuantidadeAcoesDisponiveis(Acao acaoArg, Long quantidadeArg) {
		acaoArg.setQuantidade(acaoArg.getQuantidade() - quantidadeArg);
		if (acaoArg.getQuantidade().equals(0L)) {
			this.acoes.remove(acaoArg);
		}
	}

	//Notifica quando uma acao for comprada com sucesso
	private void notificarOrdemCompraVenda(Acao acaoCompraVenda) {
		this.interesses.forEach(interesse -> {
			try {
				interesse.getCliente().notificar("A acao do seu interesse atingiu o limite", acaoCompraVenda);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
				

			/*if (
				(!interesse.getCodigo().equals(acaoCompraVenda.getCodigo()) && interesse.getPrecoMinimo() < acaoCompraVenda.getPreco()) || 
				(!interesse.getCodigo().equals(acaoCompraVenda.getCodigo()) && interesse.getPrecoMaximo() > acaoCompraVenda.getPreco()) &&
				interesse.getQuantidadeDesejada().compareTo(acaoCompraVenda.getQuantidade()) > 0
			) {
				this.notificarPacote(interesse, acaoCompraVenda);
				return;
			}*/

			/*try {
				// Consulta todas as acoes pra ver se o preco é menor ou maior do que o setado pelo interesse do cliente
				this.consultarAcaoEspecifica(interesse.getCodigo())
						.stream()
						.filter(
							acao -> (
								(acao.getCodigo().equals(acaoCompraVenda.getCodigo()) && acao.getPreco() <= interesse.getPrecoMinimo()) ||
								(acao.getCodigo().equals(acaoCompraVenda.getCodigo()) && acao.getPreco() >= interesse.getPrecoMaximo()) &&
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
			}*/
		});
		
		/*this.interesses.forEach(interesse -> {
			// Se for um interesse em pacotes, trata de maneira específica
			if (interesse.getEventoDesejado().equals(EnumDesiredEvent.PASSAGEM_E_HOSPEDAGEM)) {
				this.notificarPacote(interesse, novoVoo);
				return;
			}

			// Caso o interesse não seja compatível, não notifica
			if (!interesse.getEventoDesejado().equals(EnumDesiredEvent.SOMENTE_PASSAGEM)
					|| !interesse.getOrigem().equals(novoVoo.getOrigem())
					|| !interesse.getDestino().equals(novoVoo.getDestino())
					|| interesse.getNumeroPessoas().compareTo(novoVoo.getVagas()) > 0) {
				return;
			}

			try {
				// Para todos interesses compatíveis
				// - consulta todas as passagens cujo valor total é menor ou
				// igual ao preço
				// máximo
				// - para cada uma destas, notifica o cliente correspondente
				this.consultarPassagens(interesse.getOrigem(), interesse.getDestino(), null, null,
						interesse.getNumeroPessoas())
						.stream()
						.filter(passagem -> (passagem.getIda().getId().equals(novoVoo.getId())
								|| (passagem.getVolta() != null && passagem.getVolta().getId().equals(novoVoo.getId())))
								&& passagem.getValorTotal().compareTo(interesse.getPrecoMaximo()) <= 0)
						.forEach(passagemNotificacao -> {
							try {
								interesse.getCliente().notificar(passagemNotificacao);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						});

			} catch (RemoteException e) {
				e.printStackTrace();
			}
		});*/
	}

	//Copia uma instancia de acao para uma nova
	private Acao copiar(Acao acaoArg) {
		Acao novaInstancia = new Acao();
		novaInstancia.setCodigo(acaoArg.getCodigo());
		novaInstancia.setQuantidade(acaoArg.getQuantidade());
		novaInstancia.setPreco(acaoArg.getPreco());

		return novaInstancia;
	}

	//Copia uma instancia de interesse para uma nova
	private Interesse copiar(Interesse interesseArg) {
		Interesse novaInstancia = new Interesse();
		novaInstancia.setCliente(interesseArg.getCliente());
		novaInstancia.setCodigo(interesseArg.getCodigo());
		novaInstancia.setQuantidadeDesejada(interesseArg.getQuantidadeDesejada());
		novaInstancia.setLimiteGanho(interesseArg.getLimiteGanho());
		novaInstancia.setLimitePerda(interesseArg.getLimitePerda());

		return novaInstancia;
	}

}
