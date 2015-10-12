package ua.martynenko.chat.client;

import java.util.*;

/**
 * Created by Martynenko Yevhen on 12.10.2015.
 */
public class ClientGuiModel {

    private String newMessage;
    private final Set<String> allUserNames = new HashSet<>();

    public Set<String> getAllUserNames()
    {
        return Collections.unmodifiableSet(allUserNames);
    }

    public String getNewMessage()
    {
        return newMessage;
    }

    public void setNewMessage(String newMessage)
    {
        this.newMessage = newMessage;
    }

    public void addUser(String newUserName){
        this.allUserNames.add(newUserName);
    }

    public void deleteUser(String userName) {
        this.allUserNames.remove(userName);
    }
}
