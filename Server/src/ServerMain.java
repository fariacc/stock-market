
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
//import java.util.Scanner;

//import classes.Acao;
import implementation.ServidorImplementation;
import interfaceRmi.Server;
//import utils.Imprimir;

public class ServerMain {
	static String SERVIDOR = "SERVIDOR";
	static String IP = "127.0.0.1";
	static int PORT = 1099;

	public static void main(String[] args) throws RemoteException {
		Registry nameServer = LocateRegistry.createRegistry(PORT);
		Server servidor = null;
		try {
			servidor = new ServidorImplementation();
			nameServer.bind(SERVIDOR, servidor);
			System.out.println("Servidor inicializado");
		} catch (Exception e) {
			System.out.println("Erro ao inicializar servidor");
			e.printStackTrace();
		}

		// Responde as acoes do usuario no terminal
		/*Scanner scanner = new Scanner(System.in);
		try {
			while (true) {
				// Imprime as opcoes no terminal
				System.out.println();
				System.out.println("Escolha a opcao desejada");
				System.out.println("1 - Consultar acoes");
				System.out.println("2 - Cadastrar acao");
				System.out.println("3 - Remover acao");
				System.out.println();

				Integer opcao;
				try {
					opcao = new Integer(scanner.nextLine());
				} catch (NumberFormatException e) {
					System.out.println("Opcao invalida");
					continue;
				}

				String codigo;
				Long quantidadeDisponivel, preco;

				switch (opcao) {
				case 1:
					// Consultar acoes
					System.out.println();
					Imprimir.imprimirAcoes(servidor.consultarAcoes());
					break;
				case 2:
					// Cadastrar acao
					System.out.print("Informe o codigo da acao: ");
					codigo = scanner.nextLine();

					System.out.print("Informe a quantidade disponivel: ");
					quantidadeDisponivel = scanner.nextLong();
					scanner.nextLine();

					System.out.print("Informe o preco: ");
					preco = scanner.nextLong();
					scanner.nextLine();

					Acao acaoCadastro = new Acao();
					acaoCadastro.setCodigo(codigo);
					acaoCadastro.setQuantidade(quantidadeDisponivel);
					acaoCadastro.setPreco(preco);

					System.out.println(servidor.cadastrarAcao(acaoCadastro));
					break;
				case 3:
					// Remover acao
					System.out.print("Informe o codigo: ");
					codigo = scanner.nextLine();

					Acao acaoRemover = new Acao();
					acaoRemover.setCodigo(codigo);

					System.out.println(servidor.removerAcao(acaoRemover));
					break;
				default:
					System.out.println("Opcao invalida");
					break;
				}
			}
		} catch (Exception e) {
			scanner.close();
		}*/
	}
}
