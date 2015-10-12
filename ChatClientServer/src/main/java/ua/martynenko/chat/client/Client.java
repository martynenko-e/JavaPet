package ua.martynenko.chat.client;

import ua.martynenko.chat.Connection;
import ua.martynenko.chat.ConsoleHelper;
import ua.martynenko.chat.Message;
import ua.martynenko.chat.MessageType;

import java.io.IOException;

/**
 * Created by Martynenko Yevhen on 08.10.2015.
 */
public class Client
{
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }


    public void run() {//14.1.  Добавь метод void run()
        SocketThread socketThread = getSocketThread();//14.1.1. Создавать новый сокетный поток с помощью метода getSocketThread.
        socketThread.setDaemon(true);//14.1.2.  Помечать созданный поток как daemon
        socketThread.start();//14.1.3.  Запустить вспомогательный поток.
        try {
            synchronized (this) {
                this.wait();//14.1.4.    Заставить текущий поток ожидать
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Wait error");
            return;
        }
        if (clientConnected) {//14.1.5. После того, как поток дождался нотификации, проверь значение clientConnected
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }
        String message;
        while (clientConnected) {//14.1.6.   Считывай сообщения с консоли пока клиент подключен.
            if ((message = ConsoleHelper.readString()).equals("exit")) break; // Если будет введена команда 'exit', то выйди из цикла.
            if (shouldSentTextFromConsole()) {//14.1.7. После каждого считывания, если метод shouldSentTextFromConsole()
                sendTextMessage(message);
            }
        }
    }

    public class SocketThread extends Thread {
        //должен выводить текст message в консоль
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        //должен выводить в консоль информацию о том, что участник с именем userName присоединился к чату
        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(String.format("Участник с именем %s присоединился к чату", userName));
        }

        // должен выводить в консоль, что участник с именем userName покинул чат
        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(String.format("Участник с именем %s покинул чат", userName));
        }

        //
        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        //
        protected void clientHandshake() throws IOException, ClassNotFoundException {
            // В цикле получать сообщения, используя соединение connection
            while (true) {
               Message message = connection.receive();
                switch (message.getType()) {
                    case NAME_REQUEST: {
                        connection.send(new Message(MessageType.USER_NAME, getUserName()));
                        break;
                    }
                    case NAME_ACCEPTED: {
                        notifyConnectionStatusChanged(true);
                        return;
                    }
                    default: {
                        throw new IOException("Unexpected MessageType");
                    }
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                switch (message.getType()) {
                    case TEXT: {
                        processIncomingMessage(message.getData());
                        break;
                    }
                    case USER_ADDED: {
                        informAboutAddingNewUser(message.getData());
                        break;
                    }
                    case USER_REMOVED: {
                        informAboutDeletingNewUser(message.getData());
                        break;
                    }
                    default: {
                        throw new IOException("Unexpected MessageType");
                    }
                }
            }
        }

        @Override
        public void run()
        {
            try
            {
                java.net.Socket socket = new java.net.Socket(getServerAddress(), getServerPort());
                connection = new Connection(socket);
                this.clientHandshake();
                this.clientMainLoop();
            }
            catch (IOException | ClassNotFoundException e)
            {
                notifyConnectionStatusChanged(false);
            }
        }
    }

    // должен запросить ввод адреса сервера у пользователя и вернуть введенное значение
    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Введите адресс сервера");
        return ConsoleHelper.readString();
    }

    // должен запрашивать ввод порта сервера и возвращать его.
    protected int getServerPort(){
        ConsoleHelper.writeMessage("Введите порт сервера");
        return ConsoleHelper.readInt();
    }

    // должен запрашивать и возвращать имя пользователя.
    protected String getUserName() {
        ConsoleHelper.writeMessage("Введите имя пользователя");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSentTextFromConsole() {
        return true;
    }

    // должен создавать и возвращать новый объект класса SocketThread.
    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    // создает новое текстовое сообщение, используя переданный текст и отправляет его серверу через соединение connection.
    protected void sendTextMessage(String text) {
        Message msg = new Message(MessageType.TEXT, text);
        try {
            connection.send(msg);
        }
        catch (IOException e)
        {
            // необходимо вывести информацию об этом пользователю и присвоить false полю clientConnected.
            ConsoleHelper.writeMessage("Возникла ошибка");
            clientConnected = false;
        }

    }
}
