package model;

import java.io.Serializable;
import java.time.LocalTime;

public class Mensagem implements Serializable{
    private String tipo;
    private String conteudo;
    private LocalTime hora;
    
    public Mensagem(String tipo, String conteudo) {
        this(tipo, conteudo, null);
    }
    
    public Mensagem(String tipo, String conteudo, LocalTime hora) {
        this.tipo = tipo;
        this.conteudo = conteudo;
        this.hora = hora;
    }

    public String getTipo() {
        return tipo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public LocalTime getHora() {
        return hora;
    }
    
    public void setHora(LocalTime hora) {
        this.hora = hora;
    }
    
    public String retornarMensagemFormatada() {
        String msg = "";
        
        msg = "(" + retornarHorarioFormatado() + ")        " + getConteudo();
        
        return msg;
    }
    
    private String retornarHorarioFormatado() {
        //System.out.println("retornarHorarioFormatado chamado ... valor de hora é " + hora.toString());
        
        String horarioFormatado = "";
        
        int valor = hora.getHour();        
        if (valor < 10) horarioFormatado += "0";
        horarioFormatado += valor + ":";
        
        valor = hora.getMinute();
        if (valor < 10) horarioFormatado += "0";
        horarioFormatado += valor + ":";
        
        valor = hora.getSecond();
        if (valor < 10) horarioFormatado += "0";
        horarioFormatado += valor;
        
        return horarioFormatado;
    }
}
