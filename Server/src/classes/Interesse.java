package classes;

import java.io.Serializable;
import interfaceRmi.Cliente;

public class Interesse implements Serializable {
	private static final long serialVersionUID = 1L;

	private Cliente cliente;
	private String codigo;
	private Long quantidadeDesejada;
	private Long limiteGanho;
	private Long limitePerda;

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	
	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public Long getQuantidadeDesejada() {
		return quantidadeDesejada;
	}

	public void setQuantidadeDesejada(Long quantidadeDesejada) {
		this.quantidadeDesejada = quantidadeDesejada;
	}

	public Long getLimiteGanho() {
		return limiteGanho;
	}

	public void setLimiteGanho(Long limiteGanho) {
		this.limiteGanho = limiteGanho;
	}
	
	public Long getLimitePerda() {
		return limitePerda;
	}

	public void setLimitePerda(Long limitePerda) {
		this.limitePerda = limitePerda;
	}

}
