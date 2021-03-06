import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import classes.Acao;
import classes.Interesse;
import implementation.ClienteImplementation;
import interfaceRmi.Cliente;
import interfaceRmi.Server;
import utils.Imprimir;

public class ClienteMain {
	static String SERVIDOR = "SERVIDOR";
	static String IP = "127.0.0.1";
	static int PORT = 1099;
	
	public static void main(String[] args) throws AccessException, RemoteException, NotBoundException {
		Registry nameServer = LocateRegistry.getRegistry(IP, PORT);
		Server servidor = (Server) nameServer.lookup(SERVIDOR);

		Cliente cliente = new ClienteImplementation(servidor);

		// Responde as acoes do usuario no terminal
		Scanner scanner = new Scanner(System.in);
		try {
			while (true) {
				// Imprime as opcoes no terminal
				System.out.println();
				System.out.println("Opcoes");
				System.out.println("1 - Consultar acoes na carteira");
				System.out.println("2 - Consultar acao especifica na carteira");
				System.out.println("3 - Cadastrar acao na carteira");
				System.out.println("4 - Remover acao especifica da carteira");
				System.out.println("5 - Comprar acao");
				System.out.println("6 - Vender acao");
				System.out.println("7 - Consultar interesses");
				System.out.println("8 - Registrar interesse em acao atingindo limite de ganho ou perda");
				System.out.println("9 - Cancelar interesse");
				System.out.println("10 - Obter cotacoes");
				System.out.println("11 - Obter cotacao de uma acao especifica");
				System.out.println("12 - Inserir acao especifica em cotacoes");
				System.out.println("13 - Remover acao especifica de cotacoes");
				System.out.println();
				System.out.print("Selecione uma opcao: ");

				Integer opcao;
				try {
					opcao = new Integer(scanner.nextLine());
				} catch (NumberFormatException e) {
					System.out.println("Opcao invalida");
					continue;
				}

				String codigo;
				Long quantidadeAcao, preco, limiteGanho, limitePerda;

				switch (opcao) {

				case 1:
					// Consultar acoes da carteira

					System.out.println();
					Imprimir.imprimirAcoes(servidor.consultarCarteira(cliente));
					break;
					
				case 2:
					// Consultar acao especifica da carteira
					
					System.out.println();
					
					System.out.print("Informe o codigo: ");
					codigo = scanner.nextLine();

					System.out.println();
					Imprimir.imprimirAcoes(servidor.consultarCarteiraAcaoEspecifica(cliente, codigo));
					break;
				case 3:
					// Cadastrar acao especifica na carteira
					
					System.out.println();

					System.out.print("Informe o codigo da acao: ");
					codigo = scanner.nextLine();

					System.out.print("Informe a quantidade que voce possui: ");
					quantidadeAcao = scanner.nextLong();
					scanner.nextLine();

					System.out.print("Informe o valor unitario: ");
					preco = scanner.nextLong();
					scanner.nextLine();

					Acao acaoCadastro = new Acao();
					acaoCadastro.setCliente(cliente);
					acaoCadastro.setCodigo(codigo);
					acaoCadastro.setQuantidade(quantidadeAcao);
					acaoCadastro.setPreco(preco);

					System.out.println(servidor.cadastrarAcaoCarteira(acaoCadastro));
					break;
				case 4:
					//Remover acao especifica da carteira
					
					System.out.println();
					
					System.out.print("Informe o codigo: ");
					codigo = scanner.nextLine();

					System.out.println(servidor.removerAcaoCarteira(cliente, codigo));
					break;
				case 5:
					// Comprar acao
					Acao acaoCompra = new Acao();
					acaoCompra.setCliente(cliente);
					
					System.out.println();
					
					System.out.print("Informe o codigo: ");
					codigo = scanner.nextLine();
					acaoCompra.setCodigo(codigo);

					System.out.print("Informe a quantidade: ");
					quantidadeAcao = scanner.nextLong();
					scanner.nextLine();
					acaoCompra.setQuantidade(quantidadeAcao);
					
					System.out.print("Informe o preco a pagar: ");
					preco = scanner.nextLong();
					scanner.nextLine();
					acaoCompra.setPreco(preco);

					System.out.println(servidor.comprarAcao(acaoCompra));
					break;
				case 6:
					// Vender acao da carteira
					Acao acaoVenda = new Acao();
					acaoVenda.setCliente(cliente);
					
					System.out.println();
					
					System.out.print("Informe a acao a ser vendida: ");
					codigo = scanner.nextLine();
					acaoVenda.setCodigo(codigo);

					System.out.print("Informe a quantidade que deseja vender: ");
					quantidadeAcao = scanner.nextLong();
					scanner.nextLine();
					acaoVenda.setQuantidade(quantidadeAcao);

					System.out.print("Informe o preco: ");
					preco = scanner.nextLong();
					scanner.nextLine();
					acaoVenda.setPreco(preco);

					System.out.println(servidor.venderAcao(acaoVenda));	
					break;
				case 7:
					// Consultar interesses
					System.out.println();
					Imprimir.imprimirInteresses(servidor.consultarInteresses(cliente));
					break;
				case 8:
					// Registrar interesse
					System.out.println();

					System.out.print("Informe o codigo: ");
					codigo = scanner.nextLine();

					System.out.print("Informe a quantidade de acoes: ");
					quantidadeAcao = scanner.nextLong();
					scanner.nextLine();

					System.out.print("Informe o limite de ganho: ");
					limiteGanho = scanner.nextLong();
					scanner.nextLine();
					
					System.out.print("Informe o limite de perda: ");
					limitePerda = scanner.nextLong();
					scanner.nextLine();

					Interesse interesseCadastro = new Interesse();
					interesseCadastro.setCliente(cliente);
					interesseCadastro.setCodigo(codigo);
					interesseCadastro.setQuantidadeDesejada(quantidadeAcao);
					interesseCadastro.setLimiteGanho(limiteGanho);
					interesseCadastro.setLimitePerda(limitePerda);

					System.out.println(servidor.registrarInteresse(interesseCadastro));
					break;
				case 9:
					// Cancelar interesse
					
					System.out.println();
					
					System.out.print("Informe o codigo da acao de interesse: ");
					codigo = scanner.nextLine();

					System.out.println(servidor.removerInteresse(cliente, codigo));
					break;
				case 10:
					//Obter cotacoes
					
					System.out.println();
					Imprimir.imprimirAcoes(servidor.obterCotacoes(cliente));
					
					break;
				case 11:
					//Obter cotacao de acao especifica
					
					System.out.println();
					
					System.out.print("Informe o codigo: ");
					codigo = scanner.nextLine();

					System.out.println();
					Imprimir.imprimirAcoes(servidor.obterCotacaoAcaoEspecifica(cliente, codigo));
					break;
				case 12:
					// Inserir acao especifica na lista de cotacoes
					
					System.out.println();

					System.out.print("Informe o codigo da acao: ");
					codigo = scanner.nextLine();

					Acao cotacaoCadastro = new Acao();
					cotacaoCadastro.setCliente(cliente);
					cotacaoCadastro.setCodigo(codigo);

					System.out.println(servidor.cadastrarAcaoCotacoes(cotacaoCadastro));
					break;
				case 13:
					//Remover acao especifica de cotacoes
					
					System.out.println();
					
					System.out.print("Informe o codigo: ");
					codigo = scanner.nextLine();

					System.out.println(servidor.removerCotacaoAcaoEspecifica(cliente, codigo));
					break;
				default:
					System.out.println("Opcao invalida");
					break;
				}
			}
		} catch (Exception e) {
			scanner.close();
		}
	}

}
