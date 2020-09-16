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
		//percorre a lista da carteira de acoes e filtra a acao pelo codigo inserido pelo 
		//cliente, caso esse codigo seja igual ao da acao percorrida no momento
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
		//adiciona a acao cadastrada na lista da carteira do cliente
		this.acoes.add(novaAcao);
		return "Acao cadastrada com sucesso";
	}

	//Remove uma acao da carteira do cliente - FUNCIONANDO
	@Override
	public String removerAcaoCarteira(Cliente clienteArg, String codigoArg) throws RemoteException {
		// Busca o acao a ser removida pelo seu codigo e vê se o cliente eh o mesmo cliente que possui a acao
		Acao acaoRemover = this.acoes.stream()
			.filter(acao -> acao.getCodigo().equals(codigoArg))
			.filter(acao -> acao.getCliente().equals(clienteArg))
			.findFirst().orElse(null);

		//caso nao encontre a acao na lista
		if (acaoRemover == null) {
			return "Acao nao encontrada";
		}

		//remove a acao da lista
		this.acoes.remove(acaoRemover);

		return "Acao removida com sucesso";
	}
	
	//Compra uma acao e notifica o cliente - FUNCIONANDO
	@Override
	public synchronized String comprarAcao(Acao acaoArg) throws RemoteException {
		
		//percorre a lista de ordens de venda e ve se a acao que quer comprar existe nessa lista
		Acao acaoVendendo = this.ordensVenda.stream()
			.filter(acao -> acao.getCodigo().equals(acaoArg.getCodigo()))
			.findFirst().orElse(null);

		//caso a acao nao exista na lista de ordens de venda
		if (acaoVendendo == null) {
			return "Acao nao encontrada";
		}

		//caso a quantidade que deseja comprar seja maior do que a quantidade disponivel
		if (!quantidadeSuficienteAcao(acaoVendendo, acaoArg.getQuantidade())) {
			return "Quantidade da acao desejada nao disponivel";
		}
		
		//caso o preco que o cliente deseja pagar seja menor que o preco da acao
		if (acaoArg.getPreco() < acaoVendendo.getPreco()) {
			return "O preco unitario eh maior do que seu preco maximo a pagar";
		}
		
		//Desconta a quantidade da acao que foi comprada e atualiza com o valor que sobrou depois da compra
		acaoVendendo.setQuantidade(acaoVendendo.getQuantidade() - acaoArg.getQuantidade());
		
		//Se quantidade disponivel da acao for igual a zero, remove a acao da lista de ordens de venda
		if (acaoVendendo.getQuantidade().equals(0L)) {
			this.ordensVenda.remove(acaoVendendo);
		}
		
		//adiciona a acao que quer comprar na lista de ordens de compra
		this.ordensCompra.add(acaoArg);
		
		//atualiza o preco da acao que esta para venda com o valor pago quando o cliente comprou a acao
		acaoVendendo.setPreco(acaoArg.getPreco());
		
		//adiciona a acao comprada na carteira do cliente		
		this.acoes.add(acaoArg);
		
		//adiciona a acao comprada nas cotacoes do cliente
		this.cotacoes.add(acaoArg);
		
		//remove a acao das ordens de compra, depois de ser comprada com sucesso
		this.ordensCompra.remove(acaoArg);
		
		//notifica o cliente que comprou a acao que a acao foi comprada
		acaoArg.getCliente().notificar("A acao foi comprada com sucesso", acaoArg);
		
		//notifica o cliente que colocou a acao para venda que a acao foi vendida
		acaoVendendo.getCliente().notificar("Sua acao foi vendida com sucesso", acaoArg);
		
		//percorre a carteira do cliente e encontra a acao que foi colocada para venda
		Acao acaoCarteiraAtualizada = this.acoes.stream()
			.filter(acao -> acao.getCodigo().equals(acaoVendendo.getCodigo()))
			.filter(acao -> acao.getCliente().equals(acaoVendendo.getCliente()))
			.findFirst().orElse(null);
		
		//atualiza a quantidade disponivel da acao na carteira
		acaoCarteiraAtualizada.setQuantidade(
			(acaoCarteiraAtualizada.getQuantidade() - acaoVendendo.getQuantidade()) + (acaoVendendo.getQuantidade() - acaoArg.getQuantidade())
		);
		
		//atualiza o preco da acao na carteira, com o preco que foi pago quando outro cliente comprou a acao
		acaoCarteiraAtualizada.setPreco(acaoVendendo.getPreco());
		
		//percorre as cotacoes de todos os clientes e encontra a acao que foi colocada para venda
		Acao acaoCotacaoAtualizada = this.cotacoes.stream()
			.filter(acao -> acao.getCodigo().equals(acaoVendendo.getCodigo()))
			.findFirst().orElse(null);
			
		//atualiza a quantidade disponivel da acao na lista de cotacoes
		acaoCotacaoAtualizada.setQuantidade(acaoVendendo.getQuantidade());
				
		//atualiza o preco da acao na lista de cotacoes com o preco que foi pago quando outro cliente comprou a acao
		acaoCotacaoAtualizada.setPreco(acaoArg.getPreco());
		
		//metodo onde tem a notificacao de interesse do cliente, passando a acao atualizada como parametro
		alertarInteresses(acaoVendendo);
		
		return "";
	}
	
	//Vende uma acao e notifica o cliente - FUNCIONANDO
	@Override
	public synchronized String venderAcao(Acao acaoArg) throws RemoteException {		
		
		//percorre a carteira do cliente e ve se a acao que quer colocar para venda existe nessa lista
		Acao acaoNaCarteira = this.acoes.stream()
			.filter(acao -> acao.getCodigo().equals(acaoArg.getCodigo()))
			.filter(acao -> acao.getCliente().equals(acaoArg.getCliente()))
			.findFirst().orElse(null);
		
		//caso o cliente queira colocar para venda uma acao que nao possui
		if (acaoNaCarteira == null) {
			return "Voce nao possui essa acao na sua carteira";
		}
		
		//caso o cliente queira colocar para venda mais acoes do que possui em sua carteira
		if (!quantidadeSuficienteAcao(acaoNaCarteira, acaoArg.getQuantidade())) {
			return "Voce nao possui essa quantidade para vender";
		}
		
		//insere a acao na lista de ordens de venda
		this.ordensVenda.add(acaoArg);
		
		//notifica o cliente que colocou a acao para vender
		acaoArg.getCliente().notificar("Sua acao foi colocada pra venda", acaoArg);
		
		//metodo onde tem a notificacao do interesse do cliente, passando a acao colocada para venda como parametro
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
		// Busca o interesse do cliente a ser removido pelo codigo da acao
		Interesse interesseRemover = this.interesses.stream()
			.filter(interesse -> interesse.getCodigo().equals(codigoArg))
			.filter(interesse -> interesse.getCliente().equals(clienteArg))
			.findFirst().orElse(null);

		//caso nao exista interesse para determinada acao
		if (interesseRemover == null) {
			return "Interesse nao encontrado";
		}

		//remove o interesse em determinada acao
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
		return this.cotacoes.stream()
				.filter(cotacao -> cotacao.getCodigo().equals(codigoArg))
				.filter(cotacao -> cotacao.getCliente().equals(clienteArg))
				.map(c -> {
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
		Acao cotacaoAcao = this.ordensVenda.stream()
			.filter(cotacao -> cotacao.getCodigo().equals(cotacaoArg.getCodigo()))
			.findFirst().orElse(null);
		
		if (cotacaoAcao == null) {
			return "Nao eh possivel fazer operacoes de cotacoes dessa acao pois ela nao esta disponivel no mercado";
		}
		
		Acao novaCotacao = this.copiar(cotacaoAcao);

		//adiciona a nova cotacao na lista de cotacoes
		novaCotacao.setCliente(cotacaoArg.getCliente());
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

		for (int i = 0; i < this.interesses.size(); i++) {
			if (
				this.interesses.get(i).getCodigo().equals(acaoArg.getCodigo()) &&
				this.interesses.get(i).getQuantidadeDesejada() <= (acaoArg.getQuantidade())
			) {
				if ((acaoArg.getPreco() - this.interesses.get(i).getLimiteGanho()) >= 0){
					this.interesses.get(i).getCliente().notificar("Uma acao de seu interesse atingiu seu limite de ganho", acaoArg);
				}
				else if ((acaoArg.getPreco() - this.interesses.get(i).getLimitePerda()) <= 0) {
					this.interesses.get(i).getCliente().notificar("Uma acao de seu interesse atingiu seu limite de perda", acaoArg);
				}
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
