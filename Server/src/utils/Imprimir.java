package utils;

import java.util.List;

import classes.Acao;
import classes.Interesse;

public class Imprimir {	
	public static void imprimirAcoes(List<Acao> acoes) {
		if (acoes.isEmpty()) {
			System.out.println("Nenhuma acao encontrada");
			return;
		}

		for (Acao acao : acoes) {
			System.out.println("Codigo:\t\t" + acao.getCodigo().toString());
			System.out.println("Quantidade disponivel:\t\t" + acao.getQuantidade());
			System.out.println("Preco unitario:\tR$ " + acao.getPreco() + ",00");
		}
	}

	public static void imprimirInteresses(List<Interesse> interesses) {
		if (interesses.isEmpty()) {
			System.out.println("Nenhum interesse encontrado");
			return;
		}

		for (Interesse interesse : interesses) {
			System.out.println("Codigo:\t\t" + interesse.getCodigo());
			System.out.println("Evento:\t\t" + interesse.getEventoDesejado().toString());
			System.out.println("Quantidade desejada:\t" + interesse.getQuantidadeDesejada());
			System.out.println("Preco minimo:\tR$ " + interesse.getPrecoMinimo() + ",00");
			System.out.println("Preco maximo:\tR$ " + interesse.getPrecoMaximo() + ",00");
		}
	}
}
