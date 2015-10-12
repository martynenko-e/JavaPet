package ua.martynenko.chat;

import java.io.IOException;

import java.net.Socket;
import java.util.Map;

/**
 * Created by Martynenko Yevhen on 08.10.2015.
 */
public class Server
{
    public static void main(String[] args)
    {
        //Запрашиваем порт с помощью ConsoleHelper
        ConsoleHelper.writeMessage("Enter port server");
        int port = ConsoleHelper.readInt();

        //Создавать серверный сокет java.net.ServerSocket
        java.net.ServerSocket serverSocket = null;
        try
        {
            serverSocket = new java.net.ServerSocket(port);
            // Выводить сообщение, что сервер запущен
            ConsoleHelper.writeMessage("Сервер запущен");
            // В бесконечном цикле слушать и принимать входящие сокетные соединения серверного сокета
            while (true) {
                Socket socket = serverSocket.accept();
                // Создавать и запускать новый поток Handler, передавая в конструктор сокет
                Handler handler = new Handler(socket);
                handler.start();
                // После создания потока обработчика Handler переходить на новый шаг цикла
                continue;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            try
            {
                serverSocket.close();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
    }


    private static Map<String, Connection> connectionMap = new java.util.concurrent.ConcurrentHashMap<>();

    // отправлять сообщение  message по всем соединениям из connectionMap
    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String,Connection> con : connectionMap.entrySet()) {
            try
            {
                con.getValue().send(message);
            }
            catch (IOException e)
            {
                ConsoleHelper.writeMessage("Не смогли отправить сообщение");
            }
        }
    }

    private static class Handler extends Thread {
        private Socket socket;
        public Handler(Socket socket){
            this.socket = socket;
        }

        // Знакомство сервера с клиентом
        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            String name = "";
            while (true)
            {
                // Сформировать и отправить команду запроса имени пользователя
                connection.send(new Message(MessageType.NAME_REQUEST));

                // Получить ответ клиента
                Message msg = connection.receive();

                // Проверить, что получена команда с именем пользователя
                if (msg != null && msg.getType().equals(MessageType.USER_NAME))
                {
                    // Достать из ответа имя, проверить, что оно не пустое и пользователь с таким именем еще не подключен (используй connectionMap)
                    // Добавить нового пользователя и соединение с ним в connectionMap
                    name = msg.getData();
                    if (name != null && !name.equals("") && !connectionMap.containsKey(name))
                    {
                        connectionMap.put(name, connection);
                        break;
                        // Если какая-то проверка не прошла, заново запросить имя клиента
                    } else continue;
                } else continue;
            }
            //Отправить клиенту команду информирующую, что его имя принято
            connection.send(new Message(MessageType.NAME_ACCEPTED));

            // Вернуть принятое имя в качестве возвращаемого значения
            return name;
        }

        // отправка клиенту (новому участнику) информации об остальных клиентах (участниках) чата
        private void sendListOfUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String,Connection> con : connectionMap.entrySet()) {
                if (con.getKey() != userName)
                    connection.send(new Message(MessageType.USER_ADDED, con.getKey()));
            }
        }

        // главный цикл обработки сообщений сервером.
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                // Принимать сообщение клиента
                Message msg = connection.receive();
                // Если принятое сообщение – это текст (тип TEXT)
                if (msg.getType().equals(MessageType.TEXT)) {
                    // формировать новое текстовое сообщение путем конкатенации
                    Message tempMsg = new Message(MessageType.TEXT, userName.concat(": ").concat(msg.getData()));
                    // Отправлять сформированное сообщение всем клиентам с помощью метода sendBroadcastMessage
                    sendBroadcastMessage(tempMsg);
                    // Если принятое сообщение не является текстом, вывести сообщение об ошибке
                } else connection.send(new Message(MessageType.TEXT, "Error"));
            }
        }

        // главный метод класса Handler, который будет вызывать все вспомогательные методы

        @Override
        public void run()
        {

            // Выводить сообщение, что установлено новое соединение с удаленным адресом, который можно получить с помощью метода getRemoteSocketAddress
            ConsoleHelper.writeMessage(" установлено новое соединение с удаленным адресом: " + socket.getRemoteSocketAddress());

            // Создавать Connection, используя поле Socket
            Connection connection = null;
            String name = null;
            try {
                connection = new Connection(socket);
                // Вызывать метод, реализующий рукопожатие с клиентом, сохраняя имя нового клиента
                name = serverHandshake(connection);

                // Рассылать всем участникам чата информацию об имени присоединившегося участника (сообщение с типом USER_ADDED)
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, name));

                // Сообщать новому участнику о существующих участниках
                sendListOfUsers(connection, name);

                // Запускать главный цикл обработки сообщений сервером
                serverMainLoop(connection, name);

            }
            catch (IOException | ClassNotFoundException e) {
                // вывести в консоль информацию, что произошла ошибка при обмене данными с удаленным адресом
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");


                if (name != null) {
                    connectionMap.remove(name);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, name));
                }

            } finally {
                //Обеспечить закрытие соединения при возникновении исключения
                try {
                    connection.close();
                }catch (IOException e1) {}
            }

            ConsoleHelper.writeMessage("Cоединение с удаленным адресом закрыто.");
        }
    }
}
