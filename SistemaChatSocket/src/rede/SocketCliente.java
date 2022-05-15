package rede;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketCliente {
    private static String servidorIP; // NÃO ESCREVER IP DIRETO AQUI - Pegue em forma de input pra não vazar no repositório :D
    private static int porta = 20100;
    
    public static void main(String[] args) {
        try {
            System.out.println("Digite o IP do servidor: ");
            BufferedReader inUsuario = new BufferedReader(new InputStreamReader(System.in)); // Obter dados do usuário para enviar
            servidorIP = inUsuario.readLine();
            
            System.out.println("Tentando conectar ao servidor (" + servidorIP + ")...");
            Socket socket = new Socket(servidorIP, porta);
            
            System.out.println("Conectou-se ao servidor.");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Leitor de input recebido do socket, instanciado de um canal de entrada que é instanciado baseado no canal de fluxo de entrada de dados do soquete.
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream())); // Emissor de output enviado para o socket, instanciado de um canal de saída que é instanciado baseado no canal de fluxo de saída de dados do soquete.
            
            System.out.println("Digite seu nickname: ");
            out.println(inUsuario.readLine());
            //out.flush(); // Adiar o flush para o usuário receber a mensagem de conexão
            
            Thread threadRecebidor = new Thread(new LeitorDeSoquete(in));
            Thread threadEmissor = new Thread(new EscritorDeSoquete(out));
            
            threadRecebidor.start();
            threadEmissor.start();
            
            out.flush();
        } catch (Exception e) {
            System.out.println("ERRO (SocketClienteMelhor): " + e.getMessage());
        }
        
    }
}

class LeitorDeSoquete extends Thread {
    BufferedReader recebidor;
    
    public LeitorDeSoquete(BufferedReader recebidor) {
        this.recebidor = recebidor;
    }
    
    public void run() {
        try {
            while (true) {
                String msg = recebidor.readLine();
                System.out.println(msg);
            }
        } catch (Exception e) {
            System.out.println("ERRO LEITOR: " + e.getMessage());
        }
    }
}

class EscritorDeSoquete extends Thread {
    PrintWriter emissor;
    
    public EscritorDeSoquete (PrintWriter emissor) {
        this.emissor = emissor;
    }
    
    public void run() {
        try {
            String msg;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); // Obter dados do usuário para enviar
            
            while (true) {
                System.out.println("Digite sua mensagem:");
                msg = in.readLine();
                emissor.println(msg);
                emissor.flush();
                //System.out.println("Cliente: " + msg); // Apenas printar as mensagens do servidor
            }
        } catch (Exception e) {
            System.out.println("ERRO ESCRITOR: " + e.getMessage());
        }
    }
}
