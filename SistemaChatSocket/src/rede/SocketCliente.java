package rede;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketCliente {
    private int porta = 20100;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    
    public void conectarAoServidor(String servidorIP, String nome) throws Exception{            
        System.out.println("Tentando conectar ao servidor (" + servidorIP + ")...");
        socket = new Socket(servidorIP, porta);
            
        System.out.println("Socket criado e conectado.");
        out = new ObjectOutputStream(socket.getOutputStream()); // Out SEMPRE primeiro
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
        
        out.writeUTF(nome);            
        out.flush();
        
        System.out.println("Inicialização dos sockets + envio de nome concluídos");
    }
    
    public void encerrarConexao() throws Exception{
        socket.close();
    }
    
    public ObjectInputStream retornarCanalDeEntrada() {
        return in;
    }
    
    public ObjectOutputStream retornarCanalDeSaida() {
        return out;
    }
}