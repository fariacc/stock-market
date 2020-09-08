import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import classes.Acao;
import classes.Interesse;
import eventos.EventoEscolhido;
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
				System.out.println("1 - Consultar acoes");
				System.out.println("2 - Consultar acao especifica");
				System.out.println("3 - Comprar acao");
				System.out.println("4 - Vender acao");
				System.out.println("5 - Consultar interesses");
				System.out.println("6 - Registrar interesse");
				System.out.println("7 - Cancelar interesse");
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
				Long quantidadeAcao, preco, precoMinimo, precoMaximo;

				switch (opcao) {

				case 1:
					// Consultar acoes

					System.out.println();
					Imprimir.imprimirAcoes(
						servidor.consultarAcoes());
					System.out.println();
					break;
					
				case 2:
					// Consultar acao especifica

					System.out.print("Informe o codigo: ");
					codigo = scanner.nextLine();

					System.out.println();
					Imprimir.imprimirAcoes(servidor.consultarAcaoEspecifica(codigo));
					System.out.println();
					break;
				case 3:
					// Comprar acao
					System.out.print("Informe o codigo: ");
					codigo = scanner.nextLine();

					Acao acao = new Acao();
					acao.setCodigo(codigo);

					System.out.print("Informe a quantidade: ");
					quantidadeAcao = scanner.nextLong();
					scanner.nextLine();

					acao.setQuantidade(quantidadeAcao);
					
					System.out.print("Informe o preco maximo a pagar: ");
					preco = scanner.nextLong();
					scanner.nextLine();
					
					acao.setPreco(preco);

					System.out.println(servidor.comprarAcao(acao));

					break;
				case 4:
					// Vender acao - ainda nao implementado
					System.out.print("Informe a acao a ser vendida: ");
					codigo = scanner.nextLine();

					System.out.print("Informe a quantidade que deseja vender: ");
					quantidadeAcao = scanner.nextLong();
					scanner.nextLine();

					System.out.print("Informe o preco minimo: ");
					preco = scanner.nextLong();
					scanner.nextLine();

					System.out.println();
					break;
				case 5:
					// Consultar interesses - ta com bug
					Imprimir.imprimirInteresses(servidor.consultarInteresses(cliente));
					System.out.println();
					break;
				case 6:
					// Registrar interesse
					System.out.println();
					System.out.println("Evento");
					System.out.println("1 - Acao atingir limite de ganho ou de perda");
					System.out.println("2 - Ordem de compra ou venda concluida com sucesso");
					System.out.println();
					System.out.println("Selecione um evento desejado: ");

					int eventoInt = scanner.nextInt() - 1;
					scanner.nextLine();

					EventoEscolhido eventoDesejado = null;
					switch (eventoInt) {
					case 1:
						eventoDesejado = EventoEscolhido.ACAO_LIMITE_GANHO_PERDA;
						break;
					case 2:
						eventoDesejado = EventoEscolhido.ORDEM_COMPRA_VENDA_SUCESSO;
						break;
					default:
						break;
					}

					System.out.print("Informe o codigo: ");
					codigo = scanner.nextLine();

					System.out.print("Informe a quantidade de acoes: ");
					quantidadeAcao = scanner.nextLong();
					scanner.nextLine();

					System.out.print("Informe o preco minimo: ");
					precoMinimo = scanner.nextLong();
					scanner.nextLine();
					
					System.out.print("Informe o preco maximo: ");
					precoMaximo = scanner.nextLong();
					scanner.nextLine();

					Interesse interesseCadastro = new Interesse();
					interesseCadastro.setCodigo(codigo);
					interesseCadastro.setCliente(cliente);
					interesseCadastro.setEventoDesejado(eventoDesejado);
					interesseCadastro.setQuantidadeDesejada(quantidadeAcao);
					interesseCadastro.setPrecoMinimo(precoMinimo);
					interesseCadastro.setPrecoMaximo(precoMaximo);

					System.out.println(servidor.registrarInteresse(interesseCadastro));
					break;
				case 7:
					// Cancelar interesse
					System.out.print("Informe o codigo da acao de interesse: ");
					codigo = scanner.nextLine();

					Interesse interesseCancelamento = new Interesse();
					interesseCancelamento.setCodigo(codigo);

					System.out.println(servidor.removerInteresse(interesseCancelamento));
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
