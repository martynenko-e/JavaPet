package ua.martynenko.chat.client;

import ua.martynenko.chat.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;



/**
 * Created by Martynenko Yevhen on 12.10.2015.
 */
public class BotClient extends Client
{
    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException
        {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message)
        {
            ConsoleHelper.writeMessage(message);
            String user = "";
            String text = "";
            if (message.contains(": ")) {
                user = message.substring(0, message.indexOf(": "));
                text = message.substring(message.indexOf(": ") + 2);
            }
            SimpleDateFormat format = null;
            if ("дата".equalsIgnoreCase(text))
            {
                format = new SimpleDateFormat("d.MM.YYYY");
            } else if ("день".equalsIgnoreCase(text))
            {
                format = new SimpleDateFormat("d");
            } else if ("месяц".equalsIgnoreCase(text))
            {
                format = new SimpleDateFormat("MMMM");
            } else if ("год".equalsIgnoreCase(text))
            {
                format = new SimpleDateFormat("YYYY");
            } else if ("время".equalsIgnoreCase(text))
            {
                format = new SimpleDateFormat("H:mm:ss");
            } else if ("час".equalsIgnoreCase(text))
            {
                format = new SimpleDateFormat("H");
            } else if ("минуты".equalsIgnoreCase(text))
            {
                format = new SimpleDateFormat("m");
            } else if ("секунды".equalsIgnoreCase(text))
            {
                format = new SimpleDateFormat("s");
            }
            if (format != null)
            {
                sendTextMessage("Информация для " + user + ": " + format.format(Calendar.getInstance().getTime()));
            }
        }
    }

    @Override
    protected SocketThread getSocketThread()
    {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSentTextFromConsole()
    {
        return false;
    }

    @Override
    protected String getUserName()
    {
        String botName = String.format("date_bot_%s",(int) Math.random() * 100);
        return botName;
    }

    public static void main(String[] args)
    {
        BotClient client = new BotClient();
        client.run();
    }
}
