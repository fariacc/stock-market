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
	private List<Acao> ordensCompra;//lista de acoes que estao disponiveis para compra
	private List<Acao> ordensVenda;//lista de acoes que estao disponiveis para venda

	//Construtor do servidor
	public ServidorImplementation() throws RemoteException {
		super();

		this.acoes = new ArrayList<>();
		this.interesses = new ArrayList<>();
		this.cotacoes = new ArrayList<>();
		this.ordensCompra = new ArrayList<>();
		this.ordensVenda = new ArrayList<>();
	}
	
	//Consulta todas os acoes cadastradas na carteira do cliente - FUNCIONANDO
	@Override
	public List<Acao> consultarCarteira(Cliente clienteArg) throws RemoteException {
		return this.acoes.stream().filter(acao -> acao.getCliente().equals(clienteArg)).collect(Collectors.toList());
	}
	
	//Consulta uma acao especifica na carteira do cliente - FUNCIONANDO
	@Override
	public List<Acao> consultarCarteiraAcaoEspecifica(Cliente clienteArg, String codigoArg) throws RemoteException {
		return this.acoes.stream().filter(acao -> acao.getCodigo().equals(codigoArg)).map(a -> {
			Acao acao = this.copiar(a);
			acao.setCliente(clienteArg);
			acao.setCodigo(codigoArg);
			acao.setQuantidade(acao.getQuantidade());
			acao.setPreco(acao.getPreco());
			return acao;
		}).collect(Collectors.toList());
	}

	//Cadastra uma acao na carteira do cliente - FUNCIONANDO
	@Override
	public String cadastrarAcaoCarteira(Acao acaoArg) throws RemoteException {
		Acao novaAcao = this.copiar(acaoArg);

		this.acoes.add(novaAcao);
		return "Acao cadastrada com sucesso";
	}

	//Remove uma acao da carteira do cliente - FUNCIONANDO
	@Override
	public String removerAcaoCarteira(Cliente clienteArg, String codigoArg) throws RemoteException {
		// Busca o acao a ser removido pelo seu codigo
		Acao acaoRemover = this.acoes.stream()
			.filter(acao -> acao.getCodigo().equals(codigoArg))
			.filter(acao -> acao.getCliente().equals(clienteArg))
			.findFirst().orElse(null);

		if (acaoRemover == null) {
			return "Acao nao encontrada";
		}

		this.acoes.remove(acaoRemover);

		return "Acao removida com sucesso";
	}
	
	//Compra uma acao e notifica o cliente - FUNCIONANDO
	@Override
	public synchronized String comprarAcao(Acao acaoArg) throws RemoteException {
		this.ordensCompra.add(acaoArg);
		
		Acao acaoVendendo = this.ordensVenda.stream()
			.filter(acao -> acao.getCodigo().equals(acaoArg.getCodigo()))
			.findFirst().orElse(null);

		if (acaoVendendo == null) {
			return "Acao nao encontrada";
		}

		if (!quantidadeSuficienteAcao(acaoVendendo, acaoArg.getQuantidade())) {
			return "Quantidade da acao desejada nao disponivel";
		}
		
		if (acaoArg.getPreco() < acaoVendendo.getPreco()) {
			return "O preco unitario eh maior do que seu preco maximo a pagar";
		}
		
		//Desconta a quantidade de uma acao
		acaoVendendo.setQuantidade(acaoVendendo.getQuantidade() - acaoArg.getQuantidade());
		
		//Se quantidade for igual a zero, remove a acao da lista de ordens de venda
		if (acaoVendendo.getQuantidade().equals(0L)) {
			this.ordensVenda.remove(acaoVendendo);
		}
				
		this.acoes.add(acaoArg);
		
		this.ordensCompra.remove(acaoArg);
		
		acaoArg.getCliente().notificar("A acao foi comprada com sucesso", acaoArg);
		acaoVendendo.getCliente().notificar("Sua acao foi vendida com sucesso", acaoArg);
		
		acaoVendendo.setPreco(acaoArg.getPreco());
		
		Acao acaoAtualizada = this.acoes.stream()
			.filter(acao -> acao.getCodigo().equals(acaoVendendo.getCodigo()))
			.filter(acao -> acao.getCliente().equals(acaoVendendo.getCliente()))
			.findFirst().orElse(null);
		
		acaoAtualizada.setQuantidade(
			(acaoAtualizada.getQuantidade() - acaoVendendo.getQuantidade()) + (acaoVendendo.getQuantidade() - acaoArg.getQuantidade())
		);
		acaoAtualizada.setPreco(acaoVendendo.getPreco());
		
		alertarInteresses(acaoArg);
		
		return "";
	}
	
	//Vende uma acao e notifica o cliente - FUNCIONANDO
	@Override
	public synchronized String venderAcao(Acao acaoArg) throws RemoteException {
		this.ordensVenda.add(acaoArg);
		Acao acaoNaCarteira = this.acoes.stream()
			.filter(acao -> acao.getCodigo().equals(acaoArg.getCodigo()))
			.filter(acao -> acao.getCliente().equals(acaoArg.getCliente()))
			.findFirst().orElse(null);
		
		if (acaoNaCarteira == null) {
			return "Voce nao possui essa acao na sua carteira";
		}
		
		if (!quantidadeSuficienteAcao(acaoNaCarteira, acaoArg.getQuantidade())) {
			return "Voce nao possui essa quantidade para vender";
		}
				
		acaoArg.getCliente().notificar("Sua acao foi colocada pra venda", acaoArg);
		
		alertarInteresses(acaoArg);

		return "";
	}

	//Consulta os interesses do cliente - FUNCIONANDO
	@Override
	public List<Interesse> consultarInteresses(Cliente clienteArg) throws RemoteException {
		// Busca os interesses de cada cliente
		return this.interesses.stream().filter(interesse -> interesse.getCliente().equals(clienteArg)).collect(Collectors.toList());
	}

	//Registra interesse no evento - FUNCIONANDO
	@Override
	public String registrarInteresse(Interesse interesseArg) throws RemoteException {
		Interesse novoInteresse = this.copiar(interesseArg);

		this.interesses.add(novoInteresse);
		return "Interesse registrado com sucesso";
	}

	//Remove o interesse no evento - FUNCIONANDO
	@Override
	public String removerInteresse(Cliente clienteArg, String codigoArg) throws RemoteException {
		// Busca o interesse a ser removido pelo codigo da acao
		Interesse interesseRemover = this.interesses.stream()
			.filter(interesse -> interesse.getCodigo().equals(codigoArg))
			.filter(interesse -> interesse.getCliente().equals(clienteArg))
			.findFirst().orElse(null);

		if (interesseRemover == null) {
			return "Interesse nao encontrado";
		}

		this.interesses.remove(interesseRemover);

		return "Interesse cancelado com sucesso";
	}
	
	//Obtem cotacao de todas as acoes do cliente - FUNCIONANDO
	@Override
	public List<Acao> obterCotacoes(Cliente clienteArg) throws RemoteException {
		return this.cotacoes.stream().filter(acao -> acao.getCliente().equals(clienteArg)).collect(Collectors.toList());
	}
	
	//Obtem cotacao de uma acao especifica do cliente - FUNCIONANDO
	@Override
	public List<Acao> obterCotacaoAcaoEspecifica(Cliente clienteArg, String codigoArg) throws RemoteException {
		return this.cotacoes.stream().filter(cotacao -> cotacao.getCodigo().equals(codigoArg)).map(c -> {
			Acao cotacao = this.copiar(c);
			cotacao.setCliente(clienteArg);
			cotacao.setCodigo(codigoArg);
			cotacao.setQuantidade(cotacao.getQuantidade());
			cotacao.setPreco(cotacao.getPreco());
			return cotacao;
		}).collect(Collectors.toList());
	}
	
	//Insere uma acao na lista de cotacoes do cliente - FUNCIONANDO
	@Override
	public String cadastrarAcaoCotacoes(Acao cotacaoArg) throws RemoteException {
		Acao novaCotacao = this.copiar(cotacaoArg);

		this.cotacoes.add(novaCotacao);
		return "Cotacao de acao cadastrada com sucesso";
	}
	
	//Remove uma acao das cotacoes do cliente - FUNCIONANDO
	@Override
	public String removerCotacaoAcaoEspecifica(Cliente clienteArg, String codigoArg) throws RemoteException {
		// Busca o cotacao da acao a ser removida pelo seu codigo
		Acao cotacaoRemover = this.cotacoes.stream()
			.filter(cotacao -> cotacao.getCodigo().equals(codigoArg))
			.filter(cotacao -> cotacao.getCliente().equals(clienteArg))
			.findFirst().orElse(null);

		if (cotacaoRemover == null) {
			return "Cotacao de acao nao encontrada";
		}

		this.cotacoes.remove(cotacaoRemover);

		return "Cotacao de acao removida com sucesso";
	}	
	
	//---------------------------------------------------------

	//Valida se uma acao tem quantidade suficiente disponivel
	private Boolean quantidadeSuficienteAcao(Acao acaoArg, Long quantidadeArg) {
		return acaoArg.getQuantidade().compareTo(quantidadeArg) >= 0;
	}
	
	//Alerta aos interessados quando uma acao atingir limite de perda ou ganho
	private void alertarInteresses(Acao acaoArg) throws RemoteException{
		/*Interesse interesseEmAcao = this.interesses.stream()
			.filter(interesse -> interesse.getCodigo().equals(acaoArg.getCodigo()))
			.filter(interesse -> interesse.getQuantidadeDesejada() <= (acaoArg.getQuantidade()))
			.filter(interesse -> interesse.getLimiteGanho() >= (acaoArg.getPreco()))
			.filter(interesse -> interesse.getLimitePerda() == (acaoArg.getPreco()))
			.findFirst().orElse(null);*/

		for (int i = 0; i < this.interesses.size(); i++) {
			if (
				this.interesses.get(i).getCodigo().equals(acaoArg.getCodigo()) &&
				this.interesses.get(i).getQuantidadeDesejada() <= (acaoArg.getQuantidade()) &&
				this.interesses.get(i).getLimiteGanho() >= (acaoArg.getPreco()) ||
				this.interesses.get(i).getLimitePerda() == (acaoArg.getPreco())
			) {
				this.interesses.get(i).getCliente().notificar("A acao de seu interesse foi colocada a venda", acaoArg);
			}
		}
	}

	//Copia uma instancia de acao para uma nova
	private Acao copiar(Acao acaoArg) {
		Acao novaInstancia = new Acao();
		novaInstancia.setCliente(acaoArg.getCliente());
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
