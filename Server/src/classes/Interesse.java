package classes;

import java.io.Serializable;

import eventos.EventoEscolhido;
import interfaceRmi.Cliente;

public class Interesse implements Serializable {
	private static final long serialVersionUID = 1L;

	private String codigo;
	private Cliente cliente;
	private EventoEscolhido eventoDesejado;
	private Long quantidadeDesejada;
	private Long precoMinimo;
	private Long precoMaximo;


	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public EventoEscolhido getEventoDesejado() {
		return eventoDesejado;
	}

	public void setEventoDesejado(EventoEscolhido eventoDesejado) {
		this.eventoDesejado = eventoDesejado;
	}

	public Long getQuantidadeDesejada() {
		return quantidadeDesejada;
	}

	public void setQuantidadeDesejada(Long quantidadeDesejada) {
		this.quantidadeDesejada = quantidadeDesejada;
	}

	public Long getPrecoMinimo() {
		return precoMinimo;
	}

	public void setPrecoMinimo(Long precoMinimo) {
		this.precoMinimo = precoMinimo;
	}
	
	public Long getPrecoMaximo() {
		return precoMaximo;
	}

	public void setPrecoMaximo(Long precoMaximo) {
		this.precoMaximo = precoMaximo;
	}

}
