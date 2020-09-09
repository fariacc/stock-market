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
			System.out.println("Codigo: " + acao.getCodigo().toString());
			System.out.println("Quantidade: " + acao.getQuantidade());
			System.out.println("Preco unitario: R$ " + acao.getPreco() + ",00");
		}
	}

	public static void imprimirInteresses(List<Interesse> interesses) {
		if (interesses.isEmpty()) {
			System.out.println("Nenhum interesse encontrado");
			return;
		}

		for (Interesse interesse : interesses) {
			System.out.println("Codigo: " + interesse.getCodigo());
			System.out.println("Quantidade desejada: " + interesse.getQuantidadeDesejada());
			System.out.println("Limite de ganho: R$ " + interesse.getLimiteGanho() + ",00");
			System.out.println("Limite de perda: R$ " + interesse.getLimitePerda() + ",00");
		}
	}
}
