package classes;

import java.io.Serializable;
import interfaceRmi.Cliente;

public class Acao implements Serializable {
	private static final long serialVersionUID = 1L;

	private Cliente cliente;
	private String codigo;
	private Long quantidade;
	private Long preco;

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

	public Long getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Long quantidade) {
		this.quantidade = quantidade;
	}

	public Long getPreco() {
		return preco;
	}

	public void setPreco(Long preco) {
		this.preco = preco;
	}

}
